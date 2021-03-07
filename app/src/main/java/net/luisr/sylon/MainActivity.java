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

    List<Document> fileList = new ArrayList<>();
    RecyclerView filesRecView;
    FloatingActionButton btnAdd;
    LinearLayoutManager linearLayoutManager;
    DocsRecViewAdapter adapter;
    AppDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        filesRecView = findViewById(R.id.docsRecView);
        btnAdd = findViewById(R.id.btnAdd);

        database = AppDatabase.getInstance(this);
        fileList = database.fileDao().getAll();

        linearLayoutManager = new LinearLayoutManager(this);
        filesRecView.setLayoutManager(linearLayoutManager);
        adapter = new DocsRecViewAdapter(MainActivity.this, fileList);
        filesRecView.setAdapter(adapter);

        btnAdd.setOnClickListener(v -> {
            Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.dialog_add_file);
            int w = WindowManager.LayoutParams.MATCH_PARENT;
            int h = WindowManager.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setLayout(w, h);
            dialog.show();

            EditText edtText = dialog.findViewById(R.id.edtText);
            Button btnConfirm = dialog.findViewById(R.id.btnAdd);

            btnConfirm.setOnClickListener(dv -> {
                dialog.dismiss();
                String sName = edtText.getText().toString().trim();
                if (!sName.equals("")) {
                    Document sf = new Document();
                    sf.setName(sName);
                    database.fileDao().insert(sf);

                    fileList.clear();
                    fileList.addAll(database.fileDao().getAll());
                    adapter.notifyDataSetChanged();
                }
            });
        });
    }
}