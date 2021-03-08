package net.luisr.sylon;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class DocumentActivity extends AppCompatActivity {

    List<Page> pageList = new ArrayList<>();
    RecyclerView pagesRecView;
    FloatingActionButton btnAdd;
    FilesRecViewAdapter adapter;
    AppDatabase database;
    int documentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document);

        database = AppDatabase.getInstance(this);

        documentId = getIntent().getIntExtra("EXTRA_DOCUMENT_ID", -1);
        pageList = database.docDao().getPages(documentId);

        Document document = database.docDao().getById(documentId);
        setTitle(document.getName());

        pagesRecView = findViewById(R.id.pagesRecView);
        btnAdd = findViewById(R.id.btnAdd);

        pagesRecView.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new FilesRecViewAdapter(DocumentActivity.this, pageList);
        pagesRecView.setAdapter(adapter);

        btnAdd.setOnClickListener(v -> {
            Page page = new Page(documentId);
            database.pageDao().insert(page);

            pageList.clear();
            pageList.addAll(database.docDao().getPages(documentId));
            adapter.notifyDataSetChanged();
        });
    }
}