package com.karim.util;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

import com.karim.entity.Order;
import com.karim.entity.OrderItem;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

public class PdfUtil {

    public static byte[] generateBill(Order order) {

        try {

            Document doc = new Document(PageSize.A4, 50, 50, 50, 50);
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            PdfWriter writer = PdfWriter.getInstance(doc, out);
            doc.open();

            // ✅ FORCE WHITE BACKGROUND
            PdfContentByte canvas = writer.getDirectContentUnder();
            Rectangle rect = doc.getPageSize();
            canvas.setColorFill(Color.WHITE);
            canvas.rectangle(rect.getLeft(), rect.getBottom(),
                    rect.getWidth(), rect.getHeight());
            canvas.fill();

            DateTimeFormatter fmt =
                    DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

            // ─────────────────────────────────────
            // HEADER
            // ─────────────────────────────────────
            Font titleFont =
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, Color.BLACK);

            Paragraph title = new Paragraph("KARIM MART - TAX INVOICE", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(15f);
            doc.add(title);

            Font normalFont =
                    FontFactory.getFont(FontFactory.HELVETICA, 11, Color.BLACK);

            doc.add(new Paragraph("Order ID: " + order.getId(), normalFont));
            doc.add(new Paragraph("Date: " + order.getCreatedAt().format(fmt), normalFont));
            doc.add(new Paragraph("Payment Type: " + order.getPaymentType().name(), normalFont));
            doc.add(new Paragraph("Status: " + order.getStatus().name(), normalFont));
            doc.add(new Paragraph(" "));

            // ─────────────────────────────────────
            // ITEMS TABLE
            // ─────────────────────────────────────
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setWidths(new float[]{0.5f, 3f, 1.2f, 1f, 1.3f});

            Font headerFont =
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.BLACK);

            String[] headers = {"#", "Product", "Unit Price", "Qty", "Subtotal"};

            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
                cell.setBackgroundColor(Color.LIGHT_GRAY);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(8);
                table.addCell(cell);
            }

            Font cellFont =
                    FontFactory.getFont(FontFactory.HELVETICA, 11, Color.BLACK);

            AtomicInteger rowNum = new AtomicInteger(1);

            if (order.getItems() != null) {
                for (OrderItem item : order.getItems()) {

                    double subtotal =
                            item.getPrice() * item.getQuantity();

                    table.addCell(createCell(String.valueOf(rowNum.getAndIncrement()), cellFont, Element.ALIGN_CENTER));
                    table.addCell(createCell(item.getProductName(), cellFont, Element.ALIGN_LEFT));
                    table.addCell(createCell("Rs " + String.format("%.2f", item.getPrice()), cellFont, Element.ALIGN_RIGHT));
                    table.addCell(createCell(String.valueOf(item.getQuantity()), cellFont, Element.ALIGN_CENTER));
                    table.addCell(createCell("Rs " + String.format("%.2f", subtotal), cellFont, Element.ALIGN_RIGHT));
                }
            }

            doc.add(table);

            // ─────────────────────────────────────
            // TOTAL SECTION
            // ─────────────────────────────────────
            doc.add(new Paragraph(" "));

            Font totalFont =
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.BLACK);

            Paragraph total =
                    new Paragraph("Grand Total: Rs " +
                            String.format("%.2f", order.getTotalAmount()),
                            totalFont);

            total.setAlignment(Element.ALIGN_RIGHT);
            total.setSpacingBefore(10f);
            doc.add(total);

            doc.add(new Paragraph(" "));

            Paragraph thanks =
                    new Paragraph("Thank you for shopping with us!", totalFont);
            thanks.setAlignment(Element.ALIGN_CENTER);
            doc.add(thanks);

            doc.close();

            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("PDF generation failed", e);
        }
    }

    // ─────────────────────────────────────
    // Helper Method
    // ─────────────────────────────────────
    private static PdfPCell createCell(String text, Font font, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(align);
        cell.setPadding(8);
        return cell;
    }
}