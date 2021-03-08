package net.luisr.sylon;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    List<Document> docList = new ArrayList<>();
    RecyclerView filesRecView;
    FloatingActionButton btnAdd;
    DocsRecViewAdapter adapter;
    AppDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        filesRecView = findViewById(R.id.docsRecView);
        btnAdd = findViewById(R.id.btnAdd);

        database = AppDatabase.getInstance(this);
        docList = database.docDao().getAll();

        filesRecView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DocsRecViewAdapter(MainActivity.this, docList);
        filesRecView.setAdapter(adapter);

        btnAdd.setOnClickListener(v -> btnAddCallback());
    }

    private void btnAddCallback() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_doc);
        int w = WindowManager.LayoutParams.MATCH_PARENT;
        int h = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setLayout(w, h);
        dialog.show();

        EditText edtText = dialog.findViewById(R.id.edtText);
        Button btnConfirm = dialog.findViewById(R.id.btnAdd);

        edtText.setOnEditorActionListener((textView, i, keyEvent) -> {
            dialog.dismiss();
            btnConfirmCallback(edtText.getText().toString().trim());
            return false;
        });
        btnConfirm.setOnClickListener(v -> {
            dialog.dismiss();
            btnConfirmCallback(edtText.getText().toString().trim());
        });
    }

    private void btnConfirmCallback(String name) {
        if (!name.equals("")) {
            Document sf = new Document(name);
            database.docDao().insert(sf);

            docList.clear();
            docList.addAll(database.docDao().getAll());
            adapter.notifyDataSetChanged();
        }
    }
}