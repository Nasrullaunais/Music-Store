package com.music.musicstore.services;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.music.musicstore.dto.MonthlySalesReportData;
import com.music.musicstore.models.order.Order;
import com.music.musicstore.models.order.OrderItem;
import com.music.musicstore.repositories.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SalesReportService {

    @Autowired
    private OrderRepository orderRepository;

    /**
     * Generate monthly sales report data
     */
    public MonthlySalesReportData generateMonthlySalesData(int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime startDate = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endDate = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        List<Order> orders = orderRepository.findByOrderDateBetween(startDate, endDate);

        MonthlySalesReportData reportData = new MonthlySalesReportData();
        reportData.setMonth(month);
        reportData.setYear(year);

        // Calculate total sales
        BigDecimal totalSales = orders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        reportData.setTotalSales(totalSales);

        // Total orders
        reportData.setTotalOrders(orders.size());

        // Total unique customers
        long uniqueCustomers = orders.stream()
                .map(order -> order.getCustomer().getId())
                .distinct()
                .count();
        reportData.setTotalCustomers(uniqueCustomers);

        // Average order value
        BigDecimal avgOrderValue = orders.isEmpty() ? BigDecimal.ZERO :
                totalSales.divide(BigDecimal.valueOf(orders.size()), 2, RoundingMode.HALF_UP);
        reportData.setAverageOrderValue(avgOrderValue);

        // Top products
        Map<String, MonthlySalesReportData.ProductSalesData> productSalesMap = new HashMap<>();
        for (Order order : orders) {
            for (OrderItem item : order.getOrderItems()) {
                String productKey = item.getMusicTitle() != null ? item.getMusicTitle() : "Unknown Product";
                String artistName = item.getArtistName() != null ? item.getArtistName() : "Unknown Artist";

                productSalesMap.compute(productKey, (key, existing) -> {
                    if (existing == null) {
                        return new MonthlySalesReportData.ProductSalesData(
                                productKey, artistName, 1, item.getUnitPrice()
                        );
                    } else {
                        existing.setQuantitySold(existing.getQuantitySold() + 1);
                        existing.setRevenue(existing.getRevenue().add(item.getUnitPrice()));
                        return existing;
                    }
                });
            }
        }

        List<MonthlySalesReportData.ProductSalesData> topProducts = productSalesMap.values().stream()
                .sorted((a, b) -> Long.compare(b.getQuantitySold(), a.getQuantitySold()))
                .limit(10)
                .collect(Collectors.toList());
        reportData.setTopProducts(topProducts);

        // Daily sales breakdown
        Map<Integer, MonthlySalesReportData.DailySalesData> dailySalesMap = new TreeMap<>();
        for (Order order : orders) {
            int day = order.getOrderDate().getDayOfMonth();
            dailySalesMap.compute(day, (key, existing) -> {
                if (existing == null) {
                    return new MonthlySalesReportData.DailySalesData(day, order.getTotalAmount(), 1);
                } else {
                    existing.setSales(existing.getSales().add(order.getTotalAmount()));
                    existing.setOrders(existing.getOrders() + 1);
                    return existing;
                }
            });
        }
        reportData.setDailySales(new ArrayList<>(dailySalesMap.values()));

        return reportData;
    }

    /**
     * Generate PDF report
     */
    public byte[] generateMonthlySalesPdf(int year, int month) throws Exception {
        MonthlySalesReportData reportData = generateMonthlySalesData(year, month);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // Custom colors
        DeviceRgb headerColor = new DeviceRgb(41, 128, 185);
        DeviceRgb lightGray = new DeviceRgb(236, 240, 241);

        // Title
        Paragraph title = new Paragraph("MUSIC STORE - MONTHLY SALES REPORT")
                .setFontSize(20)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(headerColor);
        document.add(title);

        // Month and Year
        String monthName = YearMonth.of(year, month).format(DateTimeFormatter.ofPattern("MMMM yyyy"));
        Paragraph subtitle = new Paragraph(monthName)
                .setFontSize(16)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(subtitle);

        // Generation date
        Paragraph genDate = new Paragraph("Generated on: " + LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")))
                .setFontSize(10)
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginBottom(20);
        document.add(genDate);

        // Summary Section
        document.add(new Paragraph("SUMMARY")
                .setFontSize(14)
                .setBold()
                .setFontColor(headerColor)
                .setMarginTop(10));

        Table summaryTable = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1, 1}))
                .useAllAvailableWidth();

        summaryTable.addHeaderCell(createHeaderCell("Total Sales"));
        summaryTable.addHeaderCell(createHeaderCell("Total Orders"));
        summaryTable.addHeaderCell(createHeaderCell("Unique Customers"));
        summaryTable.addHeaderCell(createHeaderCell("Avg Order Value"));

        summaryTable.addCell(createDataCell("$" + reportData.getTotalSales().setScale(2, RoundingMode.HALF_UP)));
        summaryTable.addCell(createDataCell(String.valueOf(reportData.getTotalOrders())));
        summaryTable.addCell(createDataCell(String.valueOf(reportData.getTotalCustomers())));
        summaryTable.addCell(createDataCell("$" + reportData.getAverageOrderValue().setScale(2, RoundingMode.HALF_UP)));

        document.add(summaryTable);

        // Top Products Section
        document.add(new Paragraph("TOP 10 BEST SELLING PRODUCTS")
                .setFontSize(14)
                .setBold()
                .setFontColor(headerColor)
                .setMarginTop(20));

        Table productsTable = new Table(UnitValue.createPercentArray(new float[]{1, 3, 2, 2, 2}))
                .useAllAvailableWidth();

        productsTable.addHeaderCell(createHeaderCell("#"));
        productsTable.addHeaderCell(createHeaderCell("Product Name"));
        productsTable.addHeaderCell(createHeaderCell("Artist"));
        productsTable.addHeaderCell(createHeaderCell("Quantity Sold"));
        productsTable.addHeaderCell(createHeaderCell("Revenue"));

        int rank = 1;
        for (MonthlySalesReportData.ProductSalesData product : reportData.getTopProducts()) {
            productsTable.addCell(createDataCell(String.valueOf(rank++)));
            productsTable.addCell(createDataCell(product.getProductName()));
            productsTable.addCell(createDataCell(product.getArtistName()));
            productsTable.addCell(createDataCell(String.valueOf(product.getQuantitySold())));
            productsTable.addCell(createDataCell("$" + product.getRevenue().setScale(2, RoundingMode.HALF_UP)));
        }

        document.add(productsTable);

        // Daily Sales Section (Summary)
        if (!reportData.getDailySales().isEmpty()) {
            document.add(new Paragraph("DAILY SALES SUMMARY")
                    .setFontSize(14)
                    .setBold()
                    .setFontColor(headerColor)
                    .setMarginTop(20));

            Table dailyTable = new Table(UnitValue.createPercentArray(new float[]{1, 2, 2}))
                    .useAllAvailableWidth();

            dailyTable.addHeaderCell(createHeaderCell("Day"));
            dailyTable.addHeaderCell(createHeaderCell("Sales"));
            dailyTable.addHeaderCell(createHeaderCell("Orders"));

            for (MonthlySalesReportData.DailySalesData daily : reportData.getDailySales()) {
                dailyTable.addCell(createDataCell(String.valueOf(daily.getDay())));
                dailyTable.addCell(createDataCell("$" + daily.getSales().setScale(2, RoundingMode.HALF_UP)));
                dailyTable.addCell(createDataCell(String.valueOf(daily.getOrders())));
            }

            document.add(dailyTable);
        }

        // Footer
        Paragraph footer = new Paragraph("--- End of Report ---")
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(30)
                .setItalic();
        document.add(footer);

        document.close();
        return baos.toByteArray();
    }

    private Cell createHeaderCell(String content) {
        return new Cell()
                .add(new Paragraph(content).setBold().setFontColor(ColorConstants.WHITE))
                .setBackgroundColor(new DeviceRgb(41, 128, 185))
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(5);
    }

    private Cell createDataCell(String content) {
        return new Cell()
                .add(new Paragraph(content))
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(5);
    }
}

