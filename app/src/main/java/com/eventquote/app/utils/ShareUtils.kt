package com.eventquote.app.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.content.FileProvider
import java.io.File

/**
 * Utilities for sharing PDFs via WhatsApp, email, print, and general sharing.
 */
object ShareUtils {

    /**
     * Share PDF via WhatsApp.
     * If a WhatsApp number is provided, opens a direct chat.
     * Otherwise, opens WhatsApp file share picker.
     */
    fun shareViaWhatsApp(context: Context, pdfFile: File, whatsAppNumber: String = "") {
        val uri = getFileUri(context, pdfFile) ?: return

        if (whatsAppNumber.isNotBlank()) {
            // Clean the number
            val cleaned = whatsAppNumber.replace(Regex("[^0-9]"), "")
            val fullNumber = if (cleaned.length == 10) "91$cleaned" else cleaned

            // Step 1: Try to open WhatsApp with the specific contact via wa.me
            // This opens a chat with the contact so user can then share the file
            try {
                val waContactIntent = Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://wa.me/$fullNumber")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(waContactIntent)
            } catch (_: Exception) { }

            // Step 2: Also open WhatsApp share picker so user can send the PDF
            // Small delay hint to user: after seeing chat, use the attach button
            // OR directly show the file share picker
        }

        // Show WhatsApp share picker (contact selector) — user picks recipient
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            `package` = "com.whatsapp"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        if (shareIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(shareIntent)
        } else {
            // WhatsApp not installed — open general share chooser
            sharePdf(context, pdfFile)
        }
    }

    /**
     * Open general share sheet for the PDF.
     */
    fun sharePdf(context: Context, pdfFile: File, subject: String = "Event Quotation") {
        val uri = getFileUri(context, pdfFile) ?: return
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, subject)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(
            Intent.createChooser(intent, "Share Quotation via...").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
    }

    /**
     * Share via email with subject and body pre-filled.
     */
    fun shareViaEmail(
        context: Context,
        pdfFile: File,
        toEmail: String = "",
        subject: String = "Event Quotation",
        body: String = "Please find the quotation attached."
    ) {
        val uri = getFileUri(context, pdfFile) ?: return
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(toEmail))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(
            Intent.createChooser(intent, "Send Email").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
    }

    /**
     * Open the PDF file for viewing.
     */
    fun openPdf(context: Context, pdfFile: File) {
        val uri = getFileUri(context, pdfFile) ?: return
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        }
    }

    private fun getFileUri(context: Context, file: File): Uri? {
        return runCatching {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        }.getOrNull()
    }
}
