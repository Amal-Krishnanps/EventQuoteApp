package com.eventquote.app.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.graphics.Typeface
import com.eventquote.app.model.CompanySettings
import com.eventquote.app.model.Estimate
import com.eventquote.app.model.PdfTemplate
import com.eventquote.app.utils.CurrencyFormatter
import com.eventquote.app.utils.DateUtils
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

/**
 * Generates a professional PDF quotation using Android's built-in PdfDocument API.
 * Draws all content manually on Canvas with proper layout, colors, and spacing.
 */
object PdfGenerator {

    private const val PAGE_WIDTH = 595   // A4 width in points
    private const val PAGE_HEIGHT = 842  // A4 height in points
    private const val MARGIN = 36f
    private const val CONTENT_WIDTH = PAGE_WIDTH - (MARGIN * 2)

    // Template color schemes
    private data class ColorScheme(
        val primary: Int,
        val primaryDark: Int,
        val secondary: Int,
        val accent: Int
    )

    private val templateColors = mapOf(
        PdfTemplate.ELEGANT to ColorScheme(
            primary = Color.parseColor("#6C3EF4"),
            primaryDark = Color.parseColor("#4A1FD4"),
            secondary = Color.parseColor("#9B1FE8"),
            accent = Color.parseColor("#FFB800")
        ),
        PdfTemplate.MODERN to ColorScheme(
            primary = Color.parseColor("#1565C0"),
            primaryDark = Color.parseColor("#0D47A1"),
            secondary = Color.parseColor("#1976D2"),
            accent = Color.parseColor("#FF6F00")
        ),
        PdfTemplate.MINIMAL to ColorScheme(
            primary = Color.parseColor("#455A64"),
            primaryDark = Color.parseColor("#263238"),
            secondary = Color.parseColor("#607D8B"),
            accent = Color.parseColor("#546E7A")
        ),
        PdfTemplate.TRADITIONAL to ColorScheme(
            primary = Color.parseColor("#8D6E00"),
            primaryDark = Color.parseColor("#5D4E00"),
            secondary = Color.parseColor("#C8860A"),
            accent = Color.parseColor("#4E342E")
        )
    )

    /**
     * Main entry point. Generates the PDF and returns the File.
     */
    suspend fun generate(
        context: Context,
        estimate: Estimate,
        company: CompanySettings?
    ): File? = runCatching {
        val colors = templateColors[estimate.templateId] ?: templateColors[PdfTemplate.ELEGANT]!!
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        var y = drawHeader(canvas, company, colors, estimate)
        y = drawCustomerEvent(canvas, estimate, colors, y)
        y = drawServicesTable(canvas, estimate, colors, y)

        // Check if we need a new page for the summary
        if (y > PAGE_HEIGHT - 220) {
            document.finishPage(page)
            val page2Info = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 2).create()
            val page2 = document.startPage(page2Info)
            y = drawSummary(page2.canvas, estimate, colors, MARGIN + 20)
            y = drawPaymentDetails(page2.canvas, estimate, company, colors, y)
            y = drawTerms(page2.canvas, estimate, colors, y)
            drawFooter(page2.canvas, company, colors)
            document.finishPage(page2)
        } else {
            y = drawSummary(canvas, estimate, colors, y)
            y = drawPaymentDetails(canvas, estimate, company, colors, y)
            y = drawTerms(canvas, estimate, colors, y)
            drawFooter(canvas, company, colors)
            document.finishPage(page)
        }

        // Save to file
        val dir = File(context.filesDir, "pdfs").also { it.mkdirs() }
        val fileName = "quote_${estimate.estimateNumber.replace("-", "_")}_${UUID.randomUUID().toString().take(6)}.pdf"
        val file = File(dir, fileName)
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()
        file
    }.getOrNull()

    // ---- HEADER ----

    private fun drawHeader(
        canvas: Canvas, company: CompanySettings?,
        colors: ColorScheme, estimate: Estimate
    ): Float {
        // Background gradient bar
        val headerPaint = Paint().apply {
            color = colors.primary
            isAntiAlias = true
        }
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 130f, headerPaint)

        // Company Logo
        var logoX = MARGIN
        if (company?.logoPath?.isNotBlank() == true) {
            runCatching {
                val bmp = BitmapFactory.decodeFile(company.logoPath)
                if (bmp != null) {
                    val scaled = Bitmap.createScaledBitmap(bmp, 80, 80, true)
                    canvas.drawBitmap(scaled, MARGIN, 25f, null)
                    logoX = MARGIN + 90
                }
            }
        }

        // Company Name
        val companyNamePaint = Paint().apply {
            color = Color.WHITE
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText(company?.name ?: "Your Company Name", logoX, 52f, companyNamePaint)

        val companyDetailPaint = Paint().apply {
            color = Color.parseColor("#E0D8FF")
            textSize = 9f
            isAntiAlias = true
        }
        var companyY = 66f
        if (company?.phone?.isNotBlank() == true) {
            canvas.drawText("📞 ${company.phone}  |  ${company.whatsAppNumber.ifBlank { "" }}", logoX, companyY, companyDetailPaint)
            companyY += 13f
        }
        if (company?.email?.isNotBlank() == true) {
            canvas.drawText("✉ ${company.email}", logoX, companyY, companyDetailPaint)
            companyY += 13f
        }
        if (company?.address?.isNotBlank() == true) {
            canvas.drawText("📍 ${company.address}", logoX, companyY, companyDetailPaint)
            companyY += 13f
        }
        if (company?.gstNumber?.isNotBlank() == true) {
            canvas.drawText("GST: ${company.gstNumber}", logoX, companyY, companyDetailPaint)
        }

        // Quotation title (right-aligned)
        val titlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 26f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("QUOTATION", PAGE_WIDTH - MARGIN, 50f, titlePaint)

        val estDetailPaint = Paint().apply {
            color = Color.parseColor("#D0C8FF")
            textSize = 9f
            isAntiAlias = true
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("No: ${estimate.estimateNumber}", PAGE_WIDTH - MARGIN, 66f, estDetailPaint)
        canvas.drawText("Date: ${DateUtils.formatDate(estimate.quotationDate)}", PAGE_WIDTH - MARGIN, 79f, estDetailPaint)
        canvas.drawText("Valid Till: ${DateUtils.formatDate(estimate.validityDate)}", PAGE_WIDTH - MARGIN, 92f, estDetailPaint)

        // Thin accent bar at bottom of header
        val accentPaint = Paint().apply { color = colors.accent }
        canvas.drawRect(0f, 128f, PAGE_WIDTH.toFloat(), 132f, accentPaint)

        return 148f
    }

    // ---- CUSTOMER & EVENT ----

    private fun drawCustomerEvent(canvas: Canvas, estimate: Estimate, colors: ColorScheme, startY: Float): Float {
        var y = startY
        val boxPaint = Paint().apply {
            color = Color.parseColor("#F8F5FF")
            isAntiAlias = true
        }
        val borderPaint = Paint().apply {
            color = Color.parseColor("#E0D8FF")
            style = Paint.Style.STROKE
            strokeWidth = 1f
            isAntiAlias = true
        }
        val midX = PAGE_WIDTH / 2f

        // Two boxes side by side
        canvas.drawRect(MARGIN, y, midX - 4, y + 90f, boxPaint)
        canvas.drawRect(MARGIN, y, midX - 4, y + 90f, borderPaint)
        canvas.drawRect(midX + 4, y, PAGE_WIDTH - MARGIN, y + 90f, boxPaint)
        canvas.drawRect(midX + 4, y, PAGE_WIDTH - MARGIN, y + 90f, borderPaint)

        val sectionLabelPaint = Paint().apply {
            color = colors.primary
            textSize = 8f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val textPaint = Paint().apply {
            color = Color.parseColor("#333333")
            textSize = 8.5f
            isAntiAlias = true
        }
        val boldTextPaint = Paint().apply {
            color = Color.parseColor("#111111")
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        // Customer box
        canvas.drawText("CUSTOMER DETAILS", MARGIN + 8, y + 14f, sectionLabelPaint)
        canvas.drawText(estimate.customerName, MARGIN + 8, y + 28f, boldTextPaint)
        if (estimate.customerAddress.isNotBlank())
            canvas.drawText(estimate.customerAddress, MARGIN + 8, y + 41f, textPaint)
        val cityLine = listOf(estimate.customerCity, estimate.customerState, estimate.customerPincode)
            .filter { it.isNotBlank() }.joinToString(", ")
        if (cityLine.isNotBlank())
            canvas.drawText(cityLine, MARGIN + 8, y + 54f, textPaint)
        canvas.drawText("📞 ${estimate.customerMobile}", MARGIN + 8, y + 67f, textPaint)
        if (estimate.customerEmail.isNotBlank())
            canvas.drawText("✉ ${estimate.customerEmail}", MARGIN + 8, y + 80f, textPaint)

        // Event box
        canvas.drawText("EVENT DETAILS", midX + 12, y + 14f, sectionLabelPaint)
        canvas.drawText(estimate.eventType.displayName, midX + 12, y + 28f, boldTextPaint)
        canvas.drawText("Date: ${DateUtils.formatDate(estimate.functionDate)}", midX + 12, y + 41f, textPaint)
        if (estimate.functionTime.isNotBlank())
            canvas.drawText("Time: ${estimate.functionTime}", midX + 12, y + 54f, textPaint)
        canvas.drawText("Venue: ${estimate.venueName}", midX + 12, y + 67f, textPaint)
        if (estimate.guestCount > 0)
            canvas.drawText("Guests: ${estimate.guestCount}", midX + 12, y + 80f, textPaint)

        return y + 104f
    }

    // ---- SERVICES TABLE ----

    private fun drawServicesTable(canvas: Canvas, estimate: Estimate, colors: ColorScheme, startY: Float): Float {
        var y = startY

        // Table header
        val headerBgPaint = Paint().apply { color = colors.primary }
        canvas.drawRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + 22f, headerBgPaint)

        val headerTextPaint = Paint().apply {
            color = Color.WHITE
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val headerTextRightPaint = Paint().apply {
            color = Color.WHITE
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
            textAlign = Paint.Align.RIGHT
        }

        canvas.drawText("SERVICE", MARGIN + 8, y + 15f, headerTextPaint)
        canvas.drawText("DESCRIPTION / REMARKS", MARGIN + 200, y + 15f, headerTextPaint)
        canvas.drawText("AMOUNT", PAGE_WIDTH - MARGIN - 8, y + 15f, headerTextRightPaint)
        y += 22f

        // Row paints
        val rowBgAlt = Paint().apply { color = Color.parseColor("#F5F3FF") }
        val rowBg = Paint().apply { color = Color.WHITE }
        val cellTextPaint = Paint().apply {
            color = Color.parseColor("#222222")
            textSize = 8.5f
            isAntiAlias = true
        }
        val cellSubPaint = Paint().apply {
            color = Color.parseColor("#888888")
            textSize = 7.5f
            isAntiAlias = true
        }
        val amountPaint = Paint().apply {
            color = Color.parseColor("#222222")
            textSize = 8.5f
            isAntiAlias = true
            textAlign = Paint.Align.RIGHT
        }
        val linePaint = Paint().apply {
            color = Color.parseColor("#E8E0F0")
            style = Paint.Style.STROKE
            strokeWidth = 0.5f
        }

        estimate.services.forEachIndexed { idx, service ->
            val bg = if (idx % 2 == 0) rowBg else rowBgAlt
            val rowH = if (service.subItems.isEmpty()) 24f
            else 24f + (service.subItems.size * 16f)

            canvas.drawRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + rowH, bg)

            // Service name
            canvas.drawText(service.name, MARGIN + 8, y + 16f, cellTextPaint)

            // Description
            val desc = listOf(service.description, service.remarks).filter { it.isNotBlank() }.joinToString(" | ")
            if (desc.isNotBlank())
                canvas.drawText(desc.take(50), MARGIN + 200, y + 16f, cellSubPaint)

            // Amount
            canvas.drawText(CurrencyFormatter.format(service.amount), PAGE_WIDTH - MARGIN - 8, y + 16f, amountPaint)

            // Sub-items
            var subY = y + 28f
            service.subItems.forEach { sub ->
                canvas.drawText("  • ${sub.name}", MARGIN + 16, subY, cellSubPaint)
                if (sub.remarks.isNotBlank())
                    canvas.drawText(sub.remarks.take(40), MARGIN + 200, subY, cellSubPaint)
                if (sub.cost > 0)
                    canvas.drawText(CurrencyFormatter.format(sub.cost), PAGE_WIDTH - MARGIN - 8, subY,
                        Paint().apply { color = Color.parseColor("#888888"); textSize = 7.5f; isAntiAlias = true; textAlign = Paint.Align.RIGHT })
                subY += 16f
            }

            // Row border
            canvas.drawLine(MARGIN, y + rowH, PAGE_WIDTH - MARGIN, y + rowH, linePaint)
            y += rowH
        }

        return y + 12f
    }

    // ---- SUMMARY ----

    private fun drawSummary(canvas: Canvas, estimate: Estimate, colors: ColorScheme, startY: Float): Float {
        var y = startY
        val bgPaint = Paint().apply { color = Color.parseColor("#F8F5FF") }
        val borderPaint = Paint().apply {
            color = Color.parseColor("#D0C8F0")
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        val labelPaint = Paint().apply {
            color = Color.parseColor("#555555")
            textSize = 9f
            isAntiAlias = true
        }
        val valuePaint = Paint().apply {
            color = Color.parseColor("#222222")
            textSize = 9f
            isAntiAlias = true
            textAlign = Paint.Align.RIGHT
        }
        val totalPaint = Paint().apply {
            color = colors.primary
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val totalValuePaint = Paint().apply {
            color = colors.primary
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
            textAlign = Paint.Align.RIGHT
        }
        val balancePaint = Paint().apply {
            color = Color.parseColor("#C62828")
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val balanceValuePaint = Paint().apply {
            color = Color.parseColor("#C62828")
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
            textAlign = Paint.Align.RIGHT
        }

        val boxLeft = PAGE_WIDTH / 2f
        val boxRight = PAGE_WIDTH - MARGIN

        // Build summary row list
        val summaryItems = mutableListOf<Pair<String, String>>()
        summaryItems.add("Subtotal" to CurrencyFormatter.format(estimate.subtotal))
        if (estimate.discountAmount > 0)
            summaryItems.add("Discount" to "- ${CurrencyFormatter.format(estimate.discountAmount)}")
        if (estimate.taxEnabled && estimate.taxAmount > 0)
            summaryItems.add("GST (${estimate.taxRate}%)" to CurrencyFormatter.format(estimate.taxAmount))

        // Accurately compute box height:
        // padding top(16) + rows + divider(18) + grand total(20) + advance(16 if any) + balance label+line(28) + padding bottom(12)
        val advanceRows = if (estimate.totalAdvancePaid > 0) 1 else 0
        val boxHeight = 16f +
            (summaryItems.size * 18f) +
            18f +   // divider gap
            20f +   // grand total row
            (advanceRows * 16f) +
            28f +   // balance due line + text
            12f     // bottom padding

        canvas.drawRect(boxLeft, y, boxRight, y + boxHeight, bgPaint)
        canvas.drawRect(boxLeft, y, boxRight, y + boxHeight, borderPaint)

        var rowY = y + 16f
        summaryItems.forEach { (label, value) ->
            canvas.drawText(label, boxLeft + 8, rowY, labelPaint)
            canvas.drawText(value, boxRight - 8, rowY, valuePaint)
            rowY += 18f
        }

        // Divider before grand total
        canvas.drawLine(boxLeft + 8, rowY, boxRight - 8, rowY, borderPaint)
        rowY += 14f

        // Grand Total
        canvas.drawText("Grand Total", boxLeft + 8, rowY, totalPaint)
        canvas.drawText(CurrencyFormatter.format(estimate.grandTotal), boxRight - 8, rowY, totalValuePaint)
        rowY += 20f

        // Advance Paid
        if (estimate.totalAdvancePaid > 0) {
            canvas.drawText("Advance Paid", boxLeft + 8, rowY,
                Paint().apply { color = Color.parseColor("#2E7D32"); textSize = 9f; isAntiAlias = true })
            canvas.drawText("- ${CurrencyFormatter.format(estimate.totalAdvancePaid)}", boxRight - 8, rowY,
                Paint().apply { color = Color.parseColor("#2E7D32"); textSize = 9f; isAntiAlias = true; textAlign = Paint.Align.RIGHT })
            rowY += 16f
        }

        // Balance Due
        canvas.drawLine(boxLeft + 8, rowY, boxRight - 8, rowY, borderPaint)
        rowY += 14f
        canvas.drawText("Balance Due", boxLeft + 8, rowY, balancePaint)
        canvas.drawText(CurrencyFormatter.format(estimate.balanceAmount.coerceAtLeast(0.0)), boxRight - 8, rowY, balanceValuePaint)

        return y + boxHeight + 16f
    }

    // ---- PAYMENT DETAILS ----

    private fun drawPaymentDetails(
        canvas: Canvas, estimate: Estimate,
        company: CompanySettings?, colors: ColorScheme, startY: Float
    ): Float {
        if (estimate.advancePayments.isEmpty() && company?.bankName.isNullOrBlank()) return startY
        var y = startY + 8f

        val sectionPaint = Paint().apply {
            color = colors.primary
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val textPaint = Paint().apply {
            color = Color.parseColor("#444444")
            textSize = 8.5f
            isAntiAlias = true
        }

        if (estimate.advancePayments.isNotEmpty()) {
            canvas.drawText("ADVANCE PAYMENT DETAILS", MARGIN, y, sectionPaint)
            y += 14f
            estimate.advancePayments.forEach { payment ->
                canvas.drawText(
                    "• ${payment.mode.displayName}: ${CurrencyFormatter.format(payment.amount)}" +
                    (if (payment.reference.isNotBlank()) "  (Ref: ${payment.reference})" else ""),
                    MARGIN + 8, y, textPaint
                )
                y += 13f
            }
            y += 4f
        }

        // Bank Details
        if (company?.bankName?.isNotBlank() == true) {
            canvas.drawText("BANK DETAILS", MARGIN, y, sectionPaint)
            y += 14f
            canvas.drawText("Bank: ${company.bankName}", MARGIN + 8, y, textPaint); y += 13f
            canvas.drawText("A/C Holder: ${company.accountHolderName}", MARGIN + 8, y, textPaint); y += 13f
            canvas.drawText("A/C Number: ${company.accountNumber}", MARGIN + 8, y, textPaint); y += 13f
            canvas.drawText("IFSC: ${company.ifscCode}", MARGIN + 8, y, textPaint); y += 13f

            // QR Code
            if (company.qrCodePath.isNotBlank()) {
                runCatching {
                    val qrBmp = BitmapFactory.decodeFile(company.qrCodePath)
                    if (qrBmp != null) {
                        val scaled = Bitmap.createScaledBitmap(qrBmp, 70, 70, true)
                        canvas.drawBitmap(scaled, MARGIN + 8, y, null)
                        y += 80f
                    }
                }
            }
        }

        return y + 8f
    }

    // ---- TERMS ----

    private fun drawTerms(canvas: Canvas, estimate: Estimate, colors: ColorScheme, startY: Float): Float {
        if (estimate.termsConditions.isBlank()) return startY
        var y = startY + 8f

        val sectionPaint = Paint().apply {
            color = colors.primary
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val textPaint = Paint().apply {
            color = Color.parseColor("#555555")
            textSize = 7.5f
            isAntiAlias = true
        }

        canvas.drawText("TERMS & CONDITIONS", MARGIN, y, sectionPaint)
        y += 12f

        val lines = estimate.termsConditions.split("\n")
        lines.take(12).forEach { line ->
            if (line.isNotBlank()) {
                canvas.drawText(line.take(100), MARGIN + 8, y, textPaint)
                y += 11f
            }
        }
        return y + 8f
    }

    // ---- FOOTER ----

    private fun drawFooter(canvas: Canvas, company: CompanySettings?, colors: ColorScheme) {
        val footerY = PAGE_HEIGHT - 40f

        val dividerPaint = Paint().apply {
            color = colors.accent
            strokeWidth = 1.5f
        }
        canvas.drawLine(MARGIN, footerY, PAGE_WIDTH - MARGIN, footerY, dividerPaint)

        val footerPaint = Paint().apply {
            color = Color.parseColor("#888888")
            textSize = 7.5f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            "Thank you for choosing us. We look forward to making your event memorable!",
            PAGE_WIDTH / 2f, footerY + 14f, footerPaint
        )
        canvas.drawText(
            "${company?.name ?: ""} | ${company?.phone ?: ""}",
            PAGE_WIDTH / 2f, footerY + 27f, footerPaint
        )
    }
}
