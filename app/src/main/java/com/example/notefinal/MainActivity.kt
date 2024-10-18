package com.example.notefinal

import android.content.Context
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var editTextNote: EditText
    private lateinit var addButton: Button
    private lateinit var updateButton: Button
    private lateinit var deleteButton: Button
    private lateinit var noteList: ArrayList<String>
    private lateinit var arrayAdapter: ArrayAdapter<String>
    private var selectedNote: String? = null
    private var selectedPosition: Int = -1

    private lateinit var clockTextView: TextView
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        listView = findViewById(R.id.listView)
        editTextNote = findViewById(R.id.editTextNote)
        addButton = findViewById(R.id.addButton)
        updateButton = findViewById(R.id.updateButton)
        deleteButton = findViewById(R.id.deleteButton)

        val AlarmButton = findViewById<ImageButton>(R.id.Alarmbtn)

        noteList = loadNotes()
        arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, noteList)
        listView.adapter = arrayAdapter

        clockTextView = findViewById(R.id.clockTextView)


        // Set OnClickListener to move to the next page (SecondActivity)
        AlarmButton.setOnClickListener {
            // Create an intent to navigate to SecondActivity
            val intent = Intent(this, AlarmActivity::class.java)
            startActivity(intent) // Start SecondActivity
        }

        // Create a runnable to update the clock every second
        runnable = object : Runnable {
            override fun run() {
                updateClock()
                handler.postDelayed(this, 1000) // Update every second
            }
        }

        handler.post(runnable) // Start the clock

        // Add Note
        addButton.setOnClickListener {
            val note = editTextNote.text.toString()
            if (note.isNotEmpty()) {
                saveNote(note)
                editTextNote.text.clear()
                refreshList()
                Toast.makeText(this, "Note added successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Please enter a note", Toast.LENGTH_SHORT).show()
            }
        }

        // Update Note
        updateButton.setOnClickListener {
            if (selectedNote != null) {
                val updatedNote = editTextNote.text.toString()
                if (updatedNote.isNotEmpty()) {
                    updateNoteFile(selectedNote!!, updatedNote)
                    selectedNote = null
                    editTextNote.text.clear()
                    refreshList()
                    Toast.makeText(this, "Note updated", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Please enter a valid note", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please select a note to update", Toast.LENGTH_SHORT).show()
            }
        }

        // Delete Note
        deleteButton.setOnClickListener {
            if (selectedNote != null) {
                showDeleteConfirmationDialog(selectedNote!!)
            } else {
                Toast.makeText(this, "Please select a note to delete", Toast.LENGTH_SHORT).show()
            }
        }

        // List item selection
        listView.setOnItemClickListener { _, _, position, _ ->
            selectedNote = noteList[position]
            selectedPosition = position
            editTextNote.setText(selectedNote)
        }
    }

    private fun updateClock() {
        val currentTime = Calendar.getInstance().time
        val sdf = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())
        val timeString = sdf.format(currentTime)
        clockTextView.text = timeString
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable) // Stop the clock updates when activity is destroyed
    }

    private fun saveNote(note: String) {
        val filename = "note_${System.currentTimeMillis()}.txt"
        openFileOutput(filename, Context.MODE_PRIVATE).use {
            it.write(note.toByteArray())
        }
    }

    // Load all notes
    private fun loadNotes(): ArrayList<String> {
        val notes = ArrayList<String>()
        val files = fileList()
        for (file in files) {
            openFileInput(file).bufferedReader().useLines { lines ->
                notes.add(lines.joinToString("\n"))
            }
        }
        return notes
    }

    // Update a note file
    private fun updateNoteFile(oldNote: String, newNote: String) {
        val files = fileList()
        for (file in files) {
            openFileInput(file).bufferedReader().useLines { lines ->
                val content = lines.joinToString("\n")
                if (content == oldNote) {
                    deleteFile(file)
                    openFileOutput(file, Context.MODE_PRIVATE).use {
                        it.write(newNote.toByteArray())

                    }
                }
            }
        }
    }

    // Delete note
    private fun deleteNoteFile(note: String) {
        val files = fileList()
        for (file in files) {
            openFileInput(file).bufferedReader().useLines { lines ->
                val content = lines.joinToString("\n")
                if (content == note) {
                    deleteFile(file)

                }
            }
        }
    }

    // Refresh the note list
    private fun refreshList() {
        noteList.clear()
        noteList.addAll(loadNotes())
        arrayAdapter.notifyDataSetChanged()
    }

    // Confirm deletion with dialog
    private fun showDeleteConfirmationDialog(note: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete Note")
        builder.setMessage("Are you sure you want to delete this note?")
        builder.setPositiveButton("Yes") { _, _ ->
            deleteNoteFile(note)
            selectedNote = null
            editTextNote.text.clear()
            refreshList()
            Toast.makeText(this, "Note deleted", Toast.LENGTH_SHORT).show()
        }
        builder.setNegativeButton("No", null)
        builder.show()
    }
}