package com.music.musicstore.dto;

import java.math.BigDecimal;
import java.util.List;

public class MonthlySalesReportData {
    private int month;
    private int year;
    private BigDecimal totalSales;
    private long totalOrders;
    private long totalCustomers;
    private BigDecimal averageOrderValue;
    private List<ProductSalesData> topProducts;
    private List<DailySalesData> dailySales;

    public static class ProductSalesData {
        private String productName;
        private String artistName;
        private long quantitySold;
        private BigDecimal revenue;

        public ProductSalesData(String productName, String artistName, long quantitySold, BigDecimal revenue) {
            this.productName = productName;
            this.artistName = artistName;
            this.quantitySold = quantitySold;
            this.revenue = revenue;
        }

        // Getters and setters
        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public String getArtistName() {
            return artistName;
        }

        public void setArtistName(String artistName) {
            this.artistName = artistName;
        }

        public long getQuantitySold() {
            return quantitySold;
        }

        public void setQuantitySold(long quantitySold) {
            this.quantitySold = quantitySold;
        }

        public BigDecimal getRevenue() {
            return revenue;
        }

        public void setRevenue(BigDecimal revenue) {
            this.revenue = revenue;
        }
    }

    public static class DailySalesData {
        private int day;
        private BigDecimal sales;
        private long orders;

        public DailySalesData(int day, BigDecimal sales, long orders) {
            this.day = day;
            this.sales = sales;
            this.orders = orders;
        }

        // Getters and setters
        public int getDay() {
            return day;
        }

        public void setDay(int day) {
            this.day = day;
        }

        public BigDecimal getSales() {
            return sales;
        }

        public void setSales(BigDecimal sales) {
            this.sales = sales;
        }

        public long getOrders() {
            return orders;
        }

        public void setOrders(long orders) {
            this.orders = orders;
        }
    }

    // Getters and setters
    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public BigDecimal getTotalSales() {
        return totalSales;
    }

    public void setTotalSales(BigDecimal totalSales) {
        this.totalSales = totalSales;
    }

    public long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(long totalOrders) {
        this.totalOrders = totalOrders;
    }

    public long getTotalCustomers() {
        return totalCustomers;
    }

    public void setTotalCustomers(long totalCustomers) {
        this.totalCustomers = totalCustomers;
    }

    public BigDecimal getAverageOrderValue() {
        return averageOrderValue;
    }

    public void setAverageOrderValue(BigDecimal averageOrderValue) {
        this.averageOrderValue = averageOrderValue;
    }

    public List<ProductSalesData> getTopProducts() {
        return topProducts;
    }

    public void setTopProducts(List<ProductSalesData> topProducts) {
        this.topProducts = topProducts;
    }

    public List<DailySalesData> getDailySales() {
        return dailySales;
    }

    public void setDailySales(List<DailySalesData> dailySales) {
        this.dailySales = dailySales;
    }
}

