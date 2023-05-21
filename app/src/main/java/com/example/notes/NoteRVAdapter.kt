package com.example.notes

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NoteRVAdapter(val context: Context,
                  val NoteclickInterface:NoteClickInterface,
                  val NoteClickDeleteInterface:NoteClickDeleteInterface
):RecyclerView.Adapter<NoteRVAdapter.ViewHolder>(){

    private val allNotes=ArrayList<Note>()

        inner class ViewHolder(itemview:View):RecyclerView.ViewHolder(itemview){
            val noteTV =itemView.findViewById<TextView>(R.id.TVNoteTitle)
            val timeTV =itemView.findViewById<TextView>(R.id.TVTimestamp)
            val deleteIV =itemView.findViewById<ImageView>(R.id.Delete)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemview=LayoutInflater.from(parent.context).inflate(R.layout.note_rv_item,parent,false)
        return ViewHolder(itemview)
    }

    override fun getItemCount(): Int {
        return allNotes.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.noteTV.setText(allNotes.get(position).noteTitle)
        holder.timeTV.setText("Last Updated:"+allNotes.get(position).timeStamp)

        holder.deleteIV.setOnClickListener{
            NoteClickDeleteInterface.onDeleteIconClick(allNotes.get(position))
        }
        holder.itemView.setOnClickListener {
            NoteclickInterface.onNoteClick(allNotes.get(position))
        }

    }

    fun updateList(newList: List<Note>){
        allNotes.clear()
        allNotes.addAll(newList)
        notifyDataSetChanged()
    }
}

interface NoteClickDeleteInterface{
    fun onDeleteIconClick(note: Note)
}
interface NoteClickInterface{
    fun onNoteClick(note: Note)
}