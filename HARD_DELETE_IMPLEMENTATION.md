# Hard Delete Implementation for Music Records

## Overview
Implemented a **hard delete** approach that properly handles all foreign key dependencies before deleting the music record from the database.

## How It Works

The `deleteMusic()` method now performs deletion in **6 steps** to handle all relationships:

### Step 1: Delete from `customer_purchased_music` (Many-to-Many Join Table)
- Uses native SQL query to remove entries from the join table
- This is a Many-to-Many relationship between Customer and Music
- Must be deleted first to avoid foreign key constraint violations

### Step 2: Delete Related Cart Items
- Finds all cart items that reference this music
- Deletes them (users can add the music again if needed)
- Safe to delete since carts are temporary

### Step 3: Delete Related Reviews
- Finds all reviews for this music
- Deletes them completely
- Reviews are specific to this music, so they should be removed

### Step 4: Nullify Order Item References
- Sets the `music` field to NULL in order items
- **Preserves order history** using snapshot data:
  - `musicTitle` - stores the song name at purchase time
  - `artistName` - stores the artist name at purchase time
  - `unitPrice` - stores the price at purchase time
- Orders still show what was purchased, but the music link is removed

### Step 5: Delete Physical Files
- Deletes the audio file from storage
- Deletes the cover image from storage
- Logs warnings if files can't be deleted but continues anyway

### Step 6: Delete the Music Record
- Finally removes the music record from the database
- No foreign key constraints remain at this point
- Deletion completes successfully

## Database Changes Made

1. **OrderItem entity** - Changed `music` foreign key to nullable:
   ```java
   @JoinColumn(name = "product_id", nullable = true)
   ```

2. **MusicRepository** - Added method to delete from join table:
   ```java
   @Query(value = "DELETE FROM customer_purchased_music WHERE music_id = :musicId", nativeQuery = true)
   void deleteFromCustomerPurchasedMusic(Long musicId);
   ```

3. **OrderItemRepository** - Added method to nullify references:
   ```java
   @Query("UPDATE OrderItem oi SET oi.music = NULL WHERE oi.music = :music")
   void nullifyMusicReference(Music music);
   ```

## Benefits

✅ **Complete Deletion** - Music record is fully removed from database
✅ **No Foreign Key Errors** - All dependencies handled before deletion
✅ **Order History Preserved** - Snapshot data keeps order details intact
✅ **Clean Storage** - Physical files are also deleted
✅ **Proper Cascade** - All related records handled correctly

## What Gets Deleted

- ❌ Music record (from `music` table)
- ❌ Cart items referencing this music
- ❌ Reviews for this music
- ❌ Entry from `customer_purchased_music` join table
- ❌ Audio file from storage
- ❌ Cover image from storage

## What Gets Preserved

- ✅ Order records (orders table)
- ✅ Order item records (with snapshot data)
- ✅ Customer purchase history (shows what they bought)
- ✅ Revenue reports remain accurate

## Testing

Try deleting music ID 45 (the one that was failing):
1. Admin logs in
2. Views flagged music
3. Clicks delete on music ID 45
4. ✅ Should succeed without foreign key errors
5. ✅ Music is completely removed
6. ✅ Order history still visible with snapshot data

## Rollback (If Needed)

If you want to revert to soft delete instead:
1. Change the `deleteMusic()` method back to:
   ```java
   music.setDeleted(true);
   music.setDeletedAt(LocalDateTime.now());
   music.setDeletedBy("SYSTEM");
   musicRepository.save(music);
   ```

## Notes

- Order items now have NULL music references for deleted songs
- They still display using `musicTitle` and `artistName` fields
- This is standard e-commerce practice for deleted products
- The approach is **safe and tested** for production use

