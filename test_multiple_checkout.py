#!/usr/bin/env python3
"""
Test script to verify multiple music checkout functionality
"""

import requests
import json

# Base URL
BASE_URL = "http://localhost:8082"

def test_multiple_music_checkout():
    print("Testing multiple music checkout functionality...\n")
    
    # Step 1: Register a test user
    print("1. Registering test user...")
    register_data = {
        "username": "test_multiple_user",
        "password": "password123",
        "email": "test_multiple@example.com",
        "role": "CUSTOMER",
        "firstName": "Test",
        "lastName": "User"
    }
    
    try:
        register_response = requests.post(f"{BASE_URL}/api/auth/register", json=register_data)
        if register_response.status_code == 200:
            token_data = register_response.json()
            token = token_data['token']
            print(f"✓ User registered successfully. Token: {token[:20]}...")
        else:
            # Try to login if user already exists
            print("Registration failed, trying to login...")
            login_data = {"username": "test_multiple_user", "password": "password123"}
            login_response = requests.post(f"{BASE_URL}/api/auth/login", json=login_data)
            if login_response.status_code == 200:
                token_data = login_response.json()
                token = token_data['token']
                print(f"✓ User logged in successfully. Token: {token[:20]}...")
            else:
                print(f"✗ Login failed: {login_response.text}")
                return
    except Exception as e:
        print(f"✗ Registration/Login error: {e}")
        return
    
    headers = {"Authorization": f"Bearer {token}"}
    
    # Step 2: Get available music
    print("\n2. Getting available music...")
    try:
        music_response = requests.get(f"{BASE_URL}/api/music", headers=headers)
        if music_response.status_code == 200:
            music_list = music_response.json()
            if len(music_list) < 3:
                print("✗ Not enough music tracks available for testing")
                return
            print(f"✓ Found {len(music_list)} music tracks")
            # Select first 3 music tracks for testing
            selected_music = music_list[:3]
            music_ids = [music['id'] for music in selected_music]
            print(f"Selected music IDs: {music_ids}")
        else:
            print(f"✗ Failed to get music: {music_response.text}")
            return
    except Exception as e:
        print(f"✗ Error getting music: {e}")
        return
    
    # Step 3: Add multiple music items to cart
    print("\n3. Adding multiple music items to cart...")
    for music_id in music_ids:
        try:
            add_response = requests.post(f"{BASE_URL}/api/customer/cart/add/{music_id}", headers=headers)
            if add_response.status_code == 200:
                print(f"✓ Added music ID {music_id} to cart")
            else:
                print(f"✗ Failed to add music ID {music_id}: {add_response.text}")
        except Exception as e:
            print(f"✗ Error adding music ID {music_id}: {e}")
    
    # Step 4: Check cart contents
    print("\n4. Checking cart contents...")
    try:
        cart_response = requests.get(f"{BASE_URL}/api/customer/cart", headers=headers)
        if cart_response.status_code == 200:
            cart_data = cart_response.json()
            print(f"✓ Cart contains {len(cart_data.get('items', []))} items")
            print(f"Total amount: ${cart_data.get('totalAmount', 0)}")
            for item in cart_data.get('items', []):
                music = item.get('music', {})
                print(f"  - {music.get('name', 'Unknown')} by {music.get('artistUsername', 'Unknown')} (${item.get('unitPrice', 0)})")
        else:
            print(f"✗ Failed to get cart: {cart_response.text}")
            return
    except Exception as e:
        print(f"✗ Error getting cart: {e}")
        return
    
    # Step 5: Checkout with multiple items
    print("\n5. Performing checkout with multiple items...")
    try:
        checkout_response = requests.post(f"{BASE_URL}/api/customer/cart/checkout", headers=headers)
        if checkout_response.status_code == 200:
            order_data = checkout_response.json()
            print(f"✓ Checkout successful! Order ID: {order_data.get('id')}")
            print(f"Order total: ${order_data.get('totalAmount', 0)}")
            print(f"Order items count: {len(order_data.get('orderItems', []))}")
            for item in order_data.get('orderItems', []):
                print(f"  - {item.get('musicTitle', 'Unknown')} by {item.get('artistName', 'Unknown')} (${item.get('unitPrice', 0)})")
        else:
            print(f"✗ Checkout failed: {checkout_response.text}")
            return
    except Exception as e:
        print(f"✗ Error during checkout: {e}")
        return
    
    # Step 6: Verify cart is empty after checkout
    print("\n6. Verifying cart is empty after checkout...")
    try:
        cart_response = requests.get(f"{BASE_URL}/api/customer/cart", headers=headers)
        if cart_response.status_code == 200:
            cart_data = cart_response.json()
            item_count = len(cart_data.get('items', []))
            if item_count == 0:
                print("✓ Cart is empty after checkout")
            else:
                print(f"✗ Cart still contains {item_count} items after checkout")
        else:
            print(f"✗ Failed to verify cart: {cart_response.text}")
    except Exception as e:
        print(f"✗ Error verifying cart: {e}")
    
    print("\n" + "="*50)
    print("Multiple music checkout test completed!")
    print("="*50)

if __name__ == "__main__":
    test_multiple_music_checkout()