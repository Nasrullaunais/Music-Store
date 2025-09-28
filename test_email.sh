#!/bin/bash

# Email Test Script for Music Store
# This script will test email functionality by sending emails to underpantscpt@gmail.com

echo "🎵 Music Store Email Test Script 🎵"
echo "======================================"
echo ""

BASE_URL="http://localhost:8082"
EMAIL="underpantscpt@gmail.com"

echo "Testing email functionality by sending to: $EMAIL"
echo ""

# Test 1: Send a simple test email
echo "📧 Test 1: Sending simple test email..."
curl -X POST "$BASE_URL/api/test/send-simple-test?email=$EMAIL" \
     -H "Content-Type: application/json" \
     -w "\nHTTP Status: %{http_code}\n" \
     -s

echo ""
echo "----------------------------------------"
echo ""

# Test 2: Send a receipt-style test email
echo "📧 Test 2: Sending receipt-style test email..."
curl -X POST "$BASE_URL/api/test/send-test-email?email=$EMAIL" \
     -H "Content-Type: application/json" \
     -w "\nHTTP Status: %{http_code}\n" \
     -s

echo ""
echo "=========================================="
echo "✅ Email tests completed!"
echo ""
echo "If your Spring Boot application is running and email is configured correctly,"
echo "you should receive 2 test emails at: $EMAIL"
echo ""
echo "Check your inbox (and spam folder) for:"
echo "1. 'Music Store - Email Test' - Simple test email"
echo "2. 'Your Purchase Receipt - Order #TEST-[timestamp]' - Receipt-style email"
echo ""
echo "If you don't receive emails, check:"
echo "- Spring Boot application is running on port 8082"
echo "- Email configuration in application.properties"
echo "- Gmail app password is correct"
echo "- Firewall/network settings"
