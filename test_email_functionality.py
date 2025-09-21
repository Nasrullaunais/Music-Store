#!/usr/bin/env python3
"""
Test script to verify email functionality by placing an order
and sending receipt to underpantscpt@gmail.com
"""

import requests
import json
import time

# Base URL
BASE_URL = "http://localhost:8082"

def test_email_with_order():
    print("Testing email functionality with order placement...\n")

    # Step 1: Register/Login test user with your second email
    print("1. Setting up test user with your second email...")
    register_data = {
        "username": "email_test_user",
        "password": "password123",
        "email": "underpantscpt@gmail.com",  # Your second email
        "role": "CUSTOMER",
        "firstName": "Email",
        "lastName": "Test"
    }

    try:
        register_response = requests.post(f"{BASE_URL}/api/auth/register", json=register_data)
        if register_response.status_code == 200:
            token_data = register_response.json()
            token = token_data['token']
            print(f"✓ User registered successfully with underpantscpt@gmail.com")
        else:
            # Try to login if user already exists
            print("Registration failed, trying to login...")
            login_data = {"username": "email_test_user", "password": "password123"}
            login_response = requests.post(f"{BASE_URL}/api/auth/login", json=login_data)
            if login_response.status_code == 200:
                token_data = login_response.json()
                token = token_data['token']
                print(f"✓ User logged in successfully")
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
            if music_list:
                print(f"✓ Found {len(music_list)} music tracks available")
                # Get first few music IDs for testing
                music_ids = [music['id'] for music in music_list[:3]]  # Take first 3 tracks
                print(f"✓ Selected music IDs: {music_ids}")
            else:
                print("✗ No music tracks found")
                return
        else:
            print(f"✗ Failed to get music: {music_response.text}")
            return
    except Exception as e:
        print(f"✗ Error getting music: {e}")
        return

    # Step 3: Clear cart first
    print("\n3. Clearing cart...")
    try:
        clear_response = requests.post(f"{BASE_URL}/api/cart/clear", headers=headers)
        print(f"✓ Cart cleared")
    except Exception as e:
        print(f"Warning: Cart clear failed: {e}")

    # Step 4: Add music to cart
    print("\n4. Adding music to cart...")
    for music_id in music_ids:
        try:
            add_response = requests.post(f"{BASE_URL}/api/cart/add/{music_id}", headers=headers)
            if add_response.status_code == 200:
                print(f"✓ Added music {music_id} to cart")
            else:
                print(f"✗ Failed to add music {music_id}: {add_response.text}")
        except Exception as e:
            print(f"✗ Error adding music {music_id}: {e}")

    # Step 5: Check cart contents
    print("\n5. Checking cart contents...")
    try:
        cart_response = requests.get(f"{BASE_URL}/api/cart", headers=headers)
        if cart_response.status_code == 200:
            cart_data = cart_response.json()
            print(f"✓ Cart contains {len(cart_data.get('items', []))} items")
            print(f"✓ Total amount: ${cart_data.get('total', 0)}")
        else:
            print(f"✗ Failed to get cart: {cart_response.text}")
            return
    except Exception as e:
        print(f"✗ Error getting cart: {e}")
        return

    # Step 6: Proceed with checkout (this will trigger email)
    print("\n6. Proceeding with checkout (this will send email to underpantscpt@gmail.com)...")
    try:
        checkout_response = requests.post(f"{BASE_URL}/api/cart/checkout", headers=headers)
        if checkout_response.status_code == 200:
            order_data = checkout_response.json()
            print(f"✓ Checkout successful!")
            print(f"✓ Order ID: {order_data.get('orderId')}")
            print(f"✓ Order Date: {order_data.get('orderDate')}")
            print(f"✓ Total Amount: ${order_data.get('totalAmount')}")
            print(f"✓ Status: {order_data.get('status')}")
            print(f"\n📧 EMAIL SHOULD BE SENT TO: underpantscpt@gmail.com")
            print(f"📧 Check your inbox for the receipt email!")
        else:
            print(f"✗ Checkout failed: {checkout_response.text}")
            return
    except Exception as e:
        print(f"✗ Error during checkout: {e}")
        return

    print(f"\n🎉 Test completed! If email configuration is working, you should receive a receipt at underpantscpt@gmail.com")

if __name__ == "__main__":
    test_email_with_order()
