# Monthly Sales Report API Documentation

## Overview
The Monthly Sales Report feature allows administrators to generate comprehensive sales reports in PDF format or retrieve sales data in JSON format. The reports include key metrics such as total sales, top-selling products, daily sales breakdowns, and customer statistics.

## Features
- **PDF Download**: Download a professionally formatted PDF report with tables and statistics
- **JSON Data**: Retrieve sales data in JSON format for custom processing
- **Comprehensive Metrics**: Includes total sales, orders, customers, average order value
- **Top Products Analysis**: Shows the top 10 best-selling products with artist information
- **Daily Breakdown**: Day-by-day sales and order counts for the selected month

## API Endpoints

### 1. Download Monthly Sales Report (PDF)

**Endpoint:** `GET /api/admin/reports/sales/monthly`

**Description:** Generates and downloads a monthly sales report as a PDF file.

**Authentication:** Required (Admin role)

**Request Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| year | Integer | Yes | The year for the report (e.g., 2025) |
| month | Integer | Yes | The month for the report (1-12) |

**Example Request:**
```bash
curl -X GET "http://localhost:8080/api/admin/reports/sales/monthly?year=2025&month=10" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  --output sales_report_2025_10.pdf
```

**Response:**
- **Content-Type:** `application/pdf`
- **Content-Disposition:** `attachment; filename="sales_report_YYYY_MM.pdf"`
- **Status Code:** `200 OK` on success

**Error Responses:**
- `401 Unauthorized` - Missing or invalid authentication token
- `403 Forbidden` - User does not have admin privileges
- `500 Internal Server Error` - Failed to generate report

**Example Success Response:**
```
Binary PDF file download with filename: sales_report_2025_10.pdf
```

**Example Error Response:**
```json
{
  "message": "Failed to generate sales report: Invalid month value",
  "timestamp": "2025-10-20T13:45:00"
}
```

---

### 2. Get Monthly Sales Data (JSON)

**Endpoint:** `GET /api/admin/reports/sales/monthly/data`

**Description:** Retrieves monthly sales data in JSON format for custom processing or display.

**Authentication:** Required (Admin role)

**Request Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| year | Integer | Yes | The year for the report (e.g., 2025) |
| month | Integer | Yes | The month for the report (1-12) |

**Example Request:**
```bash
curl -X GET "http://localhost:8080/api/admin/reports/sales/monthly/data?year=2025&month=10" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response:**
- **Content-Type:** `application/json`
- **Status Code:** `200 OK` on success

**Example Success Response:**
```json
{
  "month": 10,
  "year": 2025,
  "totalSales": 15750.50,
  "totalOrders": 125,
  "totalCustomers": 87,
  "averageOrderValue": 126.00,
  "topProducts": [
    {
      "productName": "Summer Vibes",
      "artistName": "DJ SunnyBeats",
      "quantitySold": 45,
      "revenue": 2250.00
    },
    {
      "productName": "Night Drive",
      "artistName": "The Midnight Crew",
      "quantitySold": 38,
      "revenue": 1900.00
    },
    {
      "productName": "Ocean Waves",
      "artistName": "Calm Sounds",
      "quantitySold": 32,
      "revenue": 1600.00
    }
  ],
  "dailySales": [
    {
      "day": 1,
      "sales": 450.00,
      "orders": 5
    },
    {
      "day": 2,
      "sales": 780.50,
      "orders": 8
    },
    {
      "day": 3,
      "sales": 620.00,
      "orders": 6
    }
  ]
}
```

**Response Fields:**

| Field | Type | Description |
|-------|------|-------------|
| month | Integer | Month of the report (1-12) |
| year | Integer | Year of the report |
| totalSales | BigDecimal | Total revenue for the month |
| totalOrders | Long | Total number of orders |
| totalCustomers | Long | Number of unique customers who made purchases |
| averageOrderValue | BigDecimal | Average value per order |
| topProducts | Array | List of top 10 best-selling products |
| topProducts[].productName | String | Name of the product |
| topProducts[].artistName | String | Name of the artist |
| topProducts[].quantitySold | Long | Number of units sold |
| topProducts[].revenue | BigDecimal | Total revenue from this product |
| dailySales | Array | Day-by-day sales breakdown |
| dailySales[].day | Integer | Day of the month |
| dailySales[].sales | BigDecimal | Total sales for that day |
| dailySales[].orders | Long | Number of orders for that day |

**Error Responses:**
- `401 Unauthorized` - Missing or invalid authentication token
- `403 Forbidden` - User does not have admin privileges
- `500 Internal Server Error` - Failed to retrieve sales data

---

## PDF Report Format

The generated PDF report includes the following sections:

### 1. Header
- **Title:** "MUSIC STORE - MONTHLY SALES REPORT"
- **Subtitle:** Month and Year (e.g., "October 2025")
- **Generation Date:** Current date when the report was generated

### 2. Summary Section
A table with key metrics:
- **Total Sales:** Total revenue for the month
- **Total Orders:** Number of orders placed
- **Unique Customers:** Number of distinct customers
- **Avg Order Value:** Average value per order

### 3. Top 10 Best Selling Products
A ranked table showing:
- Rank number
- Product name
- Artist name
- Quantity sold
- Total revenue from the product

### 4. Daily Sales Summary
A detailed breakdown for each day:
- Day of the month
- Total sales for that day
- Number of orders placed

### 5. Footer
- End of report indicator

---

## Use Cases

### 1. Monthly Performance Review
```bash
# Download report for October 2025
curl -X GET "http://localhost:8080/api/admin/reports/sales/monthly?year=2025&month=10" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -o october_2025_sales.pdf
```

### 2. Dashboard Data Retrieval
```javascript
// Fetch sales data for dashboard display
fetch('http://localhost:8080/api/admin/reports/sales/monthly/data?year=2025&month=10', {
  headers: {
    'Authorization': `Bearer ${adminToken}`
  }
})
.then(response => response.json())
.then(data => {
  displaySalesMetrics(data);
  renderTopProductsChart(data.topProducts);
  renderDailySalesGraph(data.dailySales);
});
```

### 3. Automated Monthly Reports
```python
import requests
from datetime import datetime

def generate_monthly_report():
    now = datetime.now()
    year = now.year
    month = now.month - 1 if now.month > 1 else 12
    
    url = f"http://localhost:8080/api/admin/reports/sales/monthly"
    params = {"year": year, "month": month}
    headers = {"Authorization": f"Bearer {admin_token}"}
    
    response = requests.get(url, params=params, headers=headers)
    
    if response.status_code == 200:
        filename = f"monthly_report_{year}_{month:02d}.pdf"
        with open(filename, 'wb') as f:
            f.write(response.content)
        print(f"Report saved: {filename}")
    else:
        print(f"Error: {response.status_code}")

# Run monthly
generate_monthly_report()
```

---

## Audit Logging

All report generation activities are logged in the audit system:

**Download PDF Report:**
- **Action:** `DOWNLOAD_SALES_REPORT`
- **Resource Type:** `REPORT`
- **Description:** Downloaded monthly sales report for YYYY/MM

**View JSON Data:**
- **Action:** `VIEW_SALES_DATA`
- **Resource Type:** `REPORT`
- **Description:** Viewed monthly sales data for YYYY/MM

---

## Security

- **Authentication Required:** All endpoints require a valid JWT token
- **Authorization:** Only users with `ADMIN` role can access these endpoints
- **Rate Limiting:** Consider implementing rate limiting for report generation
- **Data Privacy:** Reports contain sensitive business data and should be handled securely

---

## Technical Details

### Dependencies
- **iText 7.2.5:** PDF generation library
- **Spring Boot:** REST API framework
- **Spring Security:** Authentication and authorization

### Service Layer
- **SalesReportService:** Handles data aggregation and PDF generation
- **OrderRepository:** Provides order data within date ranges

### Data Sources
The reports aggregate data from:
- `orders` table
- `order_items` table
- `customers` table
- `music` table (via order_items)

---

## Performance Considerations

1. **Large Datasets:** For months with many orders, report generation may take several seconds
2. **Caching:** Consider caching reports for past months (immutable data)
3. **Async Generation:** For very large reports, consider async processing with download links
4. **Pagination:** The JSON endpoint returns all data; consider pagination for very large datasets

---

## Future Enhancements

Potential improvements for future versions:

1. **Date Range Reports:** Custom date range selection
2. **Export Formats:** Add Excel, CSV export options
3. **Email Delivery:** Schedule and email reports automatically
4. **Comparison Reports:** Compare multiple months side-by-side
5. **Advanced Filters:** Filter by category, artist, price range
6. **Graphical Charts:** Include charts and graphs in PDF
7. **Custom Branding:** Add company logo and custom styling
8. **Report Templates:** Multiple template options

---

## Example Integration (React Frontend)

```javascript
import React, { useState } from 'react';
import axios from 'axios';

const SalesReportDownloader = () => {
  const [year, setYear] = useState(2025);
  const [month, setMonth] = useState(10);
  const [loading, setLoading] = useState(false);

  const downloadPdfReport = async () => {
    setLoading(true);
    try {
      const response = await axios.get(
        '/api/admin/reports/sales/monthly',
        {
          params: { year, month },
          headers: { 
            'Authorization': `Bearer ${localStorage.getItem('token')}` 
          },
          responseType: 'blob'
        }
      );
      
      // Create download link
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `sales_report_${year}_${month}.pdf`);
      document.body.appendChild(link);
      link.click();
      link.remove();
      
      alert('Report downloaded successfully!');
    } catch (error) {
      console.error('Download failed:', error);
      alert('Failed to download report');
    } finally {
      setLoading(false);
    }
  };

  const fetchSalesData = async () => {
    try {
      const response = await axios.get(
        '/api/admin/reports/sales/monthly/data',
        {
          params: { year, month },
          headers: { 
            'Authorization': `Bearer ${localStorage.getItem('token')}` 
          }
        }
      );
      
      console.log('Sales data:', response.data);
      return response.data;
    } catch (error) {
      console.error('Failed to fetch sales data:', error);
    }
  };

  return (
    <div className="sales-report-downloader">
      <h2>Monthly Sales Report</h2>
      <div>
        <label>
          Year:
          <input 
            type="number" 
            value={year} 
            onChange={(e) => setYear(parseInt(e.target.value))} 
          />
        </label>
        <label>
          Month:
          <select 
            value={month} 
            onChange={(e) => setMonth(parseInt(e.target.value))}
          >
            {[...Array(12)].map((_, i) => (
              <option key={i + 1} value={i + 1}>
                {new Date(2000, i).toLocaleString('default', { month: 'long' })}
              </option>
            ))}
          </select>
        </label>
      </div>
      <button 
        onClick={downloadPdfReport} 
        disabled={loading}
      >
        {loading ? 'Generating...' : 'Download PDF Report'}
      </button>
      <button onClick={fetchSalesData}>
        View Data (JSON)
      </button>
    </div>
  );
};

export default SalesReportDownloader;
```

---

## Testing

### Manual Testing

1. **Test PDF Download:**
   ```bash
   curl -X GET "http://localhost:8080/api/admin/reports/sales/monthly?year=2025&month=10" \
     -H "Authorization: Bearer YOUR_TOKEN" \
     -o test_report.pdf
   ```

2. **Test JSON Data:**
   ```bash
   curl -X GET "http://localhost:8080/api/admin/reports/sales/monthly/data?year=2025&month=10" \
     -H "Authorization: Bearer YOUR_TOKEN" | jq
   ```

3. **Test Invalid Parameters:**
   ```bash
   # Invalid month
   curl -X GET "http://localhost:8080/api/admin/reports/sales/monthly?year=2025&month=13" \
     -H "Authorization: Bearer YOUR_TOKEN"
   ```

4. **Test Unauthorized Access:**
   ```bash
   # Without token
   curl -X GET "http://localhost:8080/api/admin/reports/sales/monthly?year=2025&month=10"
   ```

---

## Troubleshooting

### Common Issues

1. **PDF Generation Fails:**
   - Check that iText dependency is properly installed
   - Verify database connection
   - Check for null values in order data

2. **Empty Reports:**
   - Verify that orders exist for the specified month
   - Check date range calculation
   - Ensure order_items are properly linked

3. **Unauthorized Errors:**
   - Verify JWT token is valid
   - Ensure user has ADMIN role
   - Check token expiration

4. **Performance Issues:**
   - Monitor query performance for large datasets
   - Consider adding database indexes
   - Implement caching for historical reports

---

## Support

For issues or questions:
- Check application logs: `logs/application.log`
- Review audit logs for admin actions
- Contact system administrator

---

**Last Updated:** October 20, 2025
**Version:** 1.0.0

