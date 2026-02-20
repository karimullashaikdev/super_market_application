package com.karim.util;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

import com.karim.entity.Order;
import com.karim.entity.OrderItem;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.LineSeparator;

public class PdfUtil {

	public static byte[] generateBill(Order order) {

		try {
			Document document = new Document(PageSize.A4, 40, 40, 50, 50);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			PdfWriter.getInstance(document, out);

			document.open();

			// ==========================
			// Fonts
			// ==========================
			Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22);
			Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
			Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 11);
			Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);

			// ==========================
			// Store Title
			// ==========================
			Paragraph storeName = new Paragraph("KARIM MART SUPERMARKET", titleFont);
			storeName.setAlignment(Element.ALIGN_CENTER);
			document.add(storeName);

			Paragraph thankYou = new Paragraph("Thank you for your purchase!", normalFont);
			thankYou.setAlignment(Element.ALIGN_CENTER);
			document.add(thankYou);

			document.add(new Paragraph(" "));

			// ==========================
			// Order Info Section
			// ==========================
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

			document.add(new Paragraph("Order ID: " + order.getId(), boldFont));
			document.add(new Paragraph("Customer ID: " + order.getUserId(), normalFont));
			document.add(new Paragraph("Order Date: " + order.getCreatedAt().format(formatter), normalFont));
			document.add(new Paragraph("Payment Status: " + order.getStatus().name(), boldFont));

			document.add(new Paragraph(" "));
			document.add(new LineSeparator());
			document.add(new Paragraph(" "));

			// ==========================
			// Product Table
			// ==========================
			PdfPTable table = new PdfPTable(4);
			table.setWidthPercentage(100);
			table.setSpacingBefore(10f);
			table.setWidths(new float[] { 3, 1, 1, 1 });

			addTableHeader(table, headerFont);

			for (OrderItem item : order.getItems()) {
				table.addCell(new Phrase(item.getProductName(), normalFont));
				table.addCell(new Phrase(String.format("₹ %.2f", item.getPrice()), normalFont));
				table.addCell(new Phrase(String.valueOf(item.getQuantity()), normalFont));
				table.addCell(new Phrase(String.format("₹ %.2f", item.getPrice() * item.getQuantity()), normalFont));
			}

			document.add(table);

			document.add(new Paragraph(" "));
			document.add(new LineSeparator());
			document.add(new Paragraph(" "));

			// ==========================
			// Total Section
			// ==========================
			Paragraph total = new Paragraph("Total Amount: ₹ " + String.format("%.2f", order.getTotalAmount()),
					titleFont);
			total.setAlignment(Element.ALIGN_RIGHT);
			document.add(total);

			document.add(new Paragraph(" "));
			document.add(new LineSeparator());
			document.add(new Paragraph(" "));

			// ==========================
			// Footer
			// ==========================
			Paragraph footer = new Paragraph(
					"This is a system generated invoice.\nFor any queries contact support@karimmart.com", normalFont);
			footer.setAlignment(Element.ALIGN_CENTER);
			document.add(footer);

			document.close();

			return out.toByteArray();

		} catch (Exception e) {
			throw new RuntimeException("PDF generation failed", e);
		}
	}

	private static void addTableHeader(PdfPTable table, Font headerFont) {

		PdfPCell cell;

		cell = new PdfPCell(new Phrase("Product", headerFont));
		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		table.addCell(cell);

		cell = new PdfPCell(new Phrase("Price", headerFont));
		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		table.addCell(cell);

		cell = new PdfPCell(new Phrase("Quantity", headerFont));
		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		table.addCell(cell);

		cell = new PdfPCell(new Phrase("Total", headerFont));
		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		table.addCell(cell);
	}
}
