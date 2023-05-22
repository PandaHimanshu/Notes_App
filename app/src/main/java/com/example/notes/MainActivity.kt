package com.example.notes


import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.example.notes.Note as Note

class MainActivity : AppCompatActivity(), NoteClickDeleteInterface, NoteClickInterface {

    private lateinit var notesRV:RecyclerView
    private lateinit var addFAB:FloatingActionButton
    private lateinit var viewModel: NoteViewModel

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        notesRV=findViewById(R.id.RVNotes)
        addFAB=findViewById(R.id.addFAB)
        notesRV.layoutManager=LinearLayoutManager(this)

        val noteRVAdapter=NoteRVAdapter(this,this,this)
        notesRV.adapter=noteRVAdapter
        viewModel= ViewModelProvider(this,ViewModelProvider.AndroidViewModelFactory.getInstance(application))[NoteViewModel::class.java]
        viewModel.allNote.observe(this) { list ->
            list?.let {
                noteRVAdapter.updateList(it)
            }
        }
        addFAB.setOnClickListener {
            val intent= Intent(this@MainActivity,AddEditNoteActivity::class.java)
            startActivity(intent)
            this.finish()
        }
        }

    override fun onDeleteIconClick(note: Note) {
          showdialogmessage(note)
    }

    override fun onNoteClick(note: Note) {
        val intent= Intent(this@MainActivity,AddEditNoteActivity::class.java)
        intent.putExtra("noteType","Edit")
        intent.putExtra("noteTitle",note.noteTitle)
        intent.putExtra("noteDescription",note.noteDescription)
        intent.putExtra("noteID",note.id)
        startActivity(intent)
        this.finish()
    }

      fun showdialogmessage(note: Note) {
        val alertDialog = AlertDialog.Builder(this)
            .setTitle("Delete Note")
            .setMessage("Are you sure you want to delete this note?")
            .setPositiveButton("Yes") { _, _ ->
                // Delete the note here
                viewModel.deleteNote(note)
                Toast.makeText(this, "${note.noteTitle} Deleted", Toast.LENGTH_LONG).show()
            }
            .setNegativeButton("No") {dialog, _ ->
                dialog.dismiss() // Dismiss the dialog
            }
            .create()

        alertDialog.show()
    }


}


