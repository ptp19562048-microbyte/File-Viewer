package com.fileviewer

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset

class MainActivity : AppCompatActivity() {

    private lateinit var selectFileButton: Button
    private lateinit var shareButton: Button
    private lateinit var openExternalButton: Button
    private lateinit var infoText: TextView
    private lateinit var previewText: TextView
    private lateinit var previewImage: ImageView

    private var selectedUri: Uri? = null
    private var selectedName: String = ""

    private val openFileLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            handleSelectedUri(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        selectFileButton = findViewById(R.id.selectFileButton)
        shareButton = findViewById(R.id.shareButton)
        openExternalButton = findViewById(R.id.openExternalButton)
        infoText = findViewById(R.id.infoText)
        previewText = findViewById(R.id.previewText)
        previewImage = findViewById(R.id.previewImage)

        selectFileButton.setOnClickListener {
            openPicker()
        }

        shareButton.setOnClickListener {
            shareSelectedFile()
        }

        openExternalButton.setOnClickListener {
            openWithSystemApp()
        }

        clearPreview()
    }

    private fun openPicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
        openFileLauncher.launch(arrayOf("*/*"))
    }

    private fun handleSelectedUri(uri: Uri) {
        val displayName = getDisplayName(uri)
        val size = getFileSize(uri)
        val extension = displayName.substringAfterLast('.', "").lowercase()

        selectedUri = uri
        selectedName = displayName

        infoText.text = "Fișier selectat: $displayName\nMărime: $size\nExtensie: .$extension"

        when {
            extension == "apk" -> showApkNote()
            extension in TEXT_EXTENSIONS -> readTextPreview(uri, extension)
            extension in IMAGE_EXTENSIONS -> loadImagePreview(uri)
            else -> showUnsupportedPreview()
        }
    }

    private fun showApkNote() {
        previewText.visibility = View.VISIBLE
        previewImage.visibility = View.GONE
        previewText.text = "APK detectat.\n\nAcesta este un pachet Android. Browserul/ aplicația nu îl instalează automat.\nPe Android, instalația se face prin sistem și poate necesita permisiunea „Instalare aplicații necunoscute”.\nPe iPhone/iPad, fișierele APK nu sunt compatibile."
    }

    private fun readTextPreview(uri: Uri, extension: String) {
        try {
            val input = contentResolver.openInputStream(uri) ?: return
            val text = input.bufferedReader(Charset.forName("UTF-8")).use(BufferedReader::readText)
            previewText.visibility = View.VISIBLE
            previewImage.visibility = View.GONE
            previewText.text = text.take(12000)
        } catch (e: Exception) {
            Log.e("FileViewer", "Text preview failed", e)
            previewText.visibility = View.VISIBLE
            previewImage.visibility = View.GONE
            previewText.text = "Fișierul a fost selectat, dar nu a putut fi citit ca text.\nÎl poți partaja sau deschide extern."
        }
    }

    private fun loadImagePreview(uri: Uri) {
        try {
            val input = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(input)
            previewImage.setImageBitmap(bitmap)
            previewText.visibility = View.GONE
            previewImage.visibility = View.VISIBLE
        } catch (e: Exception) {
            Log.e("FileViewer", "Image preview failed", e)
            previewText.visibility = View.VISIBLE
            previewImage.visibility = View.GONE
            previewText.text = "Imaginea a fost selectată, dar previzualizarea nu a putut fi generată."
        }
    }

    private fun showUnsupportedPreview() {
        previewText.visibility = View.VISIBLE
        previewImage.visibility = View.GONE
        previewText.text = "Formatul nu are o previzualizare internă.\nPoți deschide fișierul cu o aplicație de sistem sau îl poți partaja."
    }

    private fun clearPreview() {
        infoText.text = "Niciun fișier selectat. Alege un fișier pentru a începe."
        previewText.text = "" 
        previewText.visibility = View.GONE
        previewImage.visibility = View.GONE
    }

    private fun shareSelectedFile() {
        val uri = selectedUri ?: return
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = getMimeType(uri)
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, selectedName)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(shareIntent, "Trimite fișierul"))
    }

    private fun openWithSystemApp() {
        val uri = selectedUri ?: return
        val openIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, getMimeType(uri))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        if (openIntent.resolveActivity(packageManager) != null) {
            startActivity(Intent.createChooser(openIntent, "Deschide cu"))
        } else {
            previewText.visibility = View.VISIBLE
            previewImage.visibility = View.GONE
            previewText.text = "Nu a fost găsită o aplicație compatibilă pentru acest fișier."
        }
    }

    private fun getDisplayName(uri: Uri): String {
        val cursor = contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
        cursor?.use {
            val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index != -1 && it.moveToFirst()) {
                return it.getString(index)
            }
        }
        return "fișier_necunoscut"
    }

    private fun getFileSize(uri: Uri): String {
        val cursor = contentResolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)
        cursor?.use {
            val index = it.getColumnIndex(OpenableColumns.SIZE)
            if (index != -1 && it.moveToFirst()) {
                val size = it.getLong(index)
                return if (size >= 1024) {
                    val kb = size / 1024.0
                    "${"%.1f".format(kb)} KB"
                } else {
                    "$size B"
                }
            }
        }
        return "necunoscut"
    }

    private fun getMimeType(uri: Uri): String {
        val type = contentResolver.getType(uri)
        if (!type.isNullOrBlank()) return type

        val extension = selectedName.substringAfterLast('.', "").lowercase()
        val map = MimeTypeMap.getSingleton()
        return map.getMimeTypeFromExtension(extension) ?: "*/*"
    }

    companion object {
        private val TEXT_EXTENSIONS = setOf(
            "txt", "md", "json", "csv", "xml", "html", "htm", "log", "ini",
            "yaml", "yml", "toml", "js", "ts", "java", "kt", "cpp", "h", "cs",
            "py", "php", "sql", "css"
        )

        private val IMAGE_EXTENSIONS = setOf(
            "png", "jpg", "jpeg", "webp", "gif", "bmp"
        )
    }
}
