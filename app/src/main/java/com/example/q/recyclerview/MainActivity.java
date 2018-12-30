package com.example.q.recyclerview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private int[] images={
            R.drawable.image1, R.drawable.image3,
            R.drawable.image6, R.drawable.image8,
            R.drawable.image9, R.drawable.image10,
            R.drawable.image12
    };
    private RecyclerAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    ImageView selectImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        layoutManager = new GridLayoutManager(this,1,GridLayoutManager.HORIZONTAL,false);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new RecyclerAdapter(images);
        recyclerView.setAdapter(adapter);
        selectImage = (ImageView)findViewById(R.id.imageView);
        adapter.setOnItemClickListener(new RecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                selectImage.setImageResource(images[position]);
                selectImage.setVisibility(View.VISIBLE);
            }
        });
    }
}
