package mvp.oi.com.oiapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AddImages extends AppCompatActivity {

    @BindView(R.id.buttonAdd)
    Button addCard;

    @BindView(R.id.cardView)
    RecyclerView cardRecyclerView;

    List<ImageModel> imageModelList;
    LinearLayoutManager manager;
    private Bitmap bitmap;
    int currentPosition;
    RecyclerView.Adapter listAdapter;

    //premission and request
    private int PICK_IMAGE_REQUEST = 1;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_images);
        ButterKnife.bind(this);

        // intialising list of images
        imageModelList = new ArrayList<>();

        //initialising recycler view and manger
        cardRecyclerView.setHasFixedSize(true);
        manager = new LinearLayoutManager(getApplicationContext());
        manager.setOrientation(LinearLayoutManager.HORIZONTAL);
        cardRecyclerView.setLayoutManager(manager);

        //initialising custom Adapter Class
        listAdapter = new CustomAdapter(getApplication(),this);
        listAdapter.setHasStableIds(false);
        cardRecyclerView.setAdapter(listAdapter);

        // adding new card
        addCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageModelList.add(imageModelList.size(),new ImageModel());
                listAdapter.notifyItemInserted(imageModelList.size()-1);
                cardRecyclerView.scrollToPosition(imageModelList.size()-1);
            }
        });
    }

    //custom Adapter to add own cardView
    public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.cardViewHolder>{
        //intialising context to give context to cardViewHolder OnCreate
        Context context;
        Activity activity;
        public CustomAdapter(Context mContext,Activity currActivity) {
            this.context = mContext;
            this.activity = currActivity;
        }

        @Override
        public cardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.imagelist_view,parent,false);
            return new cardViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final cardViewHolder holder, int position) {
            // image present or not
            if(imageModelList.get(holder.getAdapterPosition()).getImage() != null){
                holder.addFromGallery.setVisibility(View.INVISIBLE);
                holder.cardImage.setVisibility(View.VISIBLE);
                holder.cardImage.setImageBitmap(imageModelList.get(holder.getAdapterPosition()).getImage());
            }else {
                holder.addFromGallery.setVisibility(View.VISIBLE);
                holder.cardImage.setVisibility(View .INVISIBLE);

                //adding image
                holder.addFromGallery.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //verify storage permission before adding
                        currentPosition = holder.getAdapterPosition();
                        verifyStoragePermissions(activity);
                    }
                });
            }

        }

        @Override
        public int getItemCount() {
            return imageModelList.size();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public class cardViewHolder extends  RecyclerView.ViewHolder{
            @BindView(R.id.buttonGallery)
            Button addFromGallery;

            @BindView(R.id.imageCard)
            ImageView cardImage;

            public cardViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this,itemView);
            }
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("new", currentPosition + "");
        if (requestCode == PICK_IMAGE_REQUEST && data != null && data.getData() != null) {
            Uri filePath = data.getData();
            try {
                //Getting the Bitmap from Gallery
                bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), filePath);
                //Setting the Bitmap to ImageView
                int nh = (int) (bitmap.getHeight() * (512.0 / bitmap.getWidth()));
                Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 512, nh, true);
                ImageModel object = imageModelList.get(currentPosition);
                object.setImage(scaled);
                listAdapter.notifyDataSetChanged();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //to check permissions given by user or not

    public void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }else {
            //permission already present
            getImageFromGallery();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_EXTERNAL_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    getImageFromGallery();
                } else {
                    // permission denied
                    Toast.makeText(getApplication(),"Please grant permission",Toast.LENGTH_SHORT);
                }
            }
        }
    }

    //getting image from gallery
    public void getImageFromGallery(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        Log.d("click", imageModelList.size() + " " + imageModelList.size() + "");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }
}
