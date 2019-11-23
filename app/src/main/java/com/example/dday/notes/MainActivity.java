package com.example.dday.notes;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {


    public static final int REQUEST_ADD_NOTE=1;
    public static final int REQUEST_EDIT_NOTE=2;

    private NoteViewModel noteViewModel;
    private RecyclerView recyclerView;
    private FloatingActionButton floatingActionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView=findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        final NoteAdapter noteAdapter=new NoteAdapter();
        recyclerView.setAdapter(noteAdapter);

        noteViewModel= ViewModelProviders.of(this).get(NoteViewModel.class);
        noteViewModel.getAllNotes().observe(this, new Observer<List<Note>>() {
            @Override
            public void onChanged(@Nullable List<Note> notes) {
                    noteAdapter.submitList(notes);
               // Toast.makeText(MainActivity.this, "changed", Toast.LENGTH_SHORT).show();
            }
        });

        floatingActionButton=findViewById(R.id.add_button);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this,EditAddNoteActivity.class);
                startActivityForResult(intent,REQUEST_ADD_NOTE);
            }
        });

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT |ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                    noteViewModel.delete(noteAdapter.getNoteAt(viewHolder.getAdapterPosition()));
                Toast.makeText(MainActivity.this, "Deleted a note", Toast.LENGTH_SHORT).show();
            }
        }).attachToRecyclerView(recyclerView);




        noteAdapter.setOnItemClickListener(new NoteAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Note note) {
                Intent intent=new Intent(MainActivity.this,EditAddNoteActivity.class);
                intent.putExtra(EditAddNoteActivity.EXTRA_ID,note.getId());
                intent.putExtra(EditAddNoteActivity.EXTRA_TITLE,note.getTitle());
                intent.putExtra(EditAddNoteActivity.EXTRA_DESCRIPTION,note.getDescription());
                intent.putExtra(EditAddNoteActivity.EXTRA_PRIORITY,note.getPriority());
                startActivityForResult(intent,REQUEST_EDIT_NOTE);
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_ADD_NOTE && resultCode ==RESULT_OK)
        {
            String title=data.getStringExtra(EditAddNoteActivity.EXTRA_TITLE);
            String description=data.getStringExtra(EditAddNoteActivity.EXTRA_DESCRIPTION);
            int priority=data.getIntExtra(EditAddNoteActivity.EXTRA_PRIORITY,1);

            Note note=new Note(title,description,priority);
            noteViewModel.insert(note);
            Toast.makeText(this, "Note saved", Toast.LENGTH_SHORT).show();
        }else if(requestCode == REQUEST_EDIT_NOTE && resultCode ==RESULT_OK){
            int id=data.getIntExtra(EditAddNoteActivity.EXTRA_ID,-1);

            if(id == -1)
            {
                Toast.makeText(this, "Note can't be updated", Toast.LENGTH_SHORT).show();
                return;
            }else {
                String title=data.getStringExtra(EditAddNoteActivity.EXTRA_TITLE);
                String description=data.getStringExtra(EditAddNoteActivity.EXTRA_DESCRIPTION);
                int priority=data.getIntExtra(EditAddNoteActivity.EXTRA_PRIORITY,1);

                Note note=new Note(title,description,priority);
                note.setId(id);
                noteViewModel.update(note);
                Toast.makeText(this, "Note Updated", Toast.LENGTH_SHORT).show();
            }
        }

        else{
            Toast.makeText(this, "Note not saved", Toast.LENGTH_SHORT).show();
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater=getMenuInflater();
        menuInflater.inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId())
        {
            case R.id.delete_all_notes :
                noteViewModel.deleteAllNotes();
                Toast.makeText(this, "All notes deleted", Toast.LENGTH_SHORT).show();
                return true;
                default:
                return super.onOptionsItemSelected(item);
        }

    }
}
