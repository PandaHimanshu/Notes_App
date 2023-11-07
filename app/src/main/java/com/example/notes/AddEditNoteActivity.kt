package com.example.notes

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.Html
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ImageSpan
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class AddEditNoteActivity : AppCompatActivity() {

    private lateinit var noteTitleEdt: EditText
    private lateinit var noteDescriptionEdt: EditText
    private lateinit var checkIcon: ImageView
    private lateinit var backbtn: ImageView
    private lateinit var viewModel: NoteViewModel
    private lateinit var bold: ImageView
    private lateinit var italic: ImageView
    private lateinit var sharebtn: ImageView
    private lateinit var photo: ImageView
    private var noteID = -1
    private var noteType: String = ""
    private var isBold = false
    private var isItalic = false
    private val PICK_IMAGE_REQUEST = 1

    @SuppressLint("MissingInflatedId", "SetTextI18n", "SimpleDateFormat", "ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_note)

        noteTitleEdt = findViewById(R.id.EdNoteTitle)
        noteDescriptionEdt = findViewById(R.id.EdNoteDescription)
        checkIcon = findViewById(R.id.checkbtn)
        backbtn = findViewById(R.id.backbtn)
        bold = findViewById(R.id.bold)
        italic = findViewById(R.id.italic)
        sharebtn = findViewById(R.id.share)
        photo = findViewById(R.id.photo)

        val boldTypeface = Typeface.defaultFromStyle(Typeface.BOLD)
        val italicTypeface = Typeface.defaultFromStyle(Typeface.ITALIC)
        val normalTypeface = Typeface.defaultFromStyle(Typeface.NORMAL)

        photo.setOnClickListener {
            openImagePicker()
        }
        bold.setOnClickListener {
            isBold = !isBold

            if (isBold) {
                noteDescriptionEdt.setTypeface(boldTypeface)
                bold.setBackgroundColor(R.color.black)
            } else {
                noteDescriptionEdt.setTypeface(normalTypeface)
                bold.setBackgroundColor(0)
            }
        }
        italic.setOnClickListener {
            isItalic = !isItalic

            if (isItalic) {
                noteDescriptionEdt.setTypeface(italicTypeface)
                italic.setBackgroundColor(R.color.black)
            } else {
                noteDescriptionEdt.setTypeface(normalTypeface)
                italic.setBackgroundColor(0)
            }
        }
        sharebtn.setOnClickListener {
            val noteTitle = noteTitleEdt.text.toString()
            val noteDescription = noteDescriptionEdt.text.toString()

            if (noteTitle.isNotEmpty() && noteDescription.isNotEmpty()) {
                val noteContent = "Title: $noteTitle\nDescription: $noteDescription"

                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, noteContent)
                }

                if (shareIntent.resolveActivity(packageManager) != null) {
                    startActivity(Intent.createChooser(shareIntent, "Share Note"))
                } else {
                    Toast.makeText(this, "No app can handle this action.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please fill in both title and description.", Toast.LENGTH_LONG).show()
            }
        }

        backbtn.setOnClickListener {
            val intent = Intent(this@AddEditNoteActivity, MainActivity::class.java)
            startActivity(intent)
        }
        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[NoteViewModel::class.java]

        noteType = intent.getStringExtra("noteType") ?: "Add"

        if (noteType == "Edit") {
            val noteTitle = intent.getStringExtra("noteTitle")
            val noteDesc = intent.getStringExtra("noteDescription")
            noteID = intent.getIntExtra("noteID", -1)
            checkIcon.setImageResource(R.drawable.baseline_check_24)
            noteTitleEdt.setText(noteTitle)

            // Load the HTML content into the EditText
            val spannedText = noteDesc?.let { fromHtml(it) }
            noteDescriptionEdt.text = SpannableStringBuilder(spannedText)
        }

        checkIcon.setOnClickListener {
            val noteTitle = noteTitleEdt.text.toString()

            // Convert the content from HTML to plain text
            val noteDescription = toHtml(noteDescriptionEdt.text).toString()

            if (noteTitle.isNotEmpty() && noteDescription.isNotEmpty()) {
                val sdf = SimpleDateFormat("yyyy MM dd - hh:mm:ss")
                val currentDate: String = sdf.format(Date())

                if (noteType == "Edit") {
                    val updateNote = Note(noteTitle, noteDescription, currentDate)
                    updateNote.id = noteID
                    viewModel.updateNote(updateNote)
                    Toast.makeText(this, "Note Updated", Toast.LENGTH_LONG).show()
                } else {
                    viewModel.addNote(Note(noteTitle, noteDescription, currentDate))
                    Toast.makeText(this, "Note Added..", Toast.LENGTH_LONG).show()
                }

                startActivity(Intent(applicationContext, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Please fill in both title and description.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun openImagePicker() {
        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            if (data != null && data.data != null) {
                try {
                    val imageUri = data.data

                    val imageTag = "<img src=\"$imageUri\" alt=\"inserted image\">"

                    val ssb = SpannableStringBuilder(noteDescriptionEdt.text)
                    val start = ssb.length
                    val end = start + imageTag.length
                    ssb.append(imageTag)

                    val drawable = Drawable.createFromStream(
                        contentResolver.openInputStream(imageUri!!),
                        imageUri.toString()
                    )
                    if (drawable != null) {
                        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
                    }
                    val imageSpan = drawable?.let { ImageSpan(it, ImageSpan.ALIGN_BASELINE) }

                    ssb.setSpan(imageSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                    noteDescriptionEdt.text.clear()
                    noteDescriptionEdt.text = ssb
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    // Function to convert HTML string to a Spanned object
    @Suppress("DEPRECATION")
    private fun fromHtml(html: String): Spanned {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(html)
        }
    }

    // Function to convert Spanned object to HTML string
    private fun toHtml(spanned: Spanned): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.toHtml(spanned, Html.TO_HTML_PARAGRAPH_LINES_INDIVIDUAL)
        } else {
            Html.toHtml(spanned)
        }
    }
}
