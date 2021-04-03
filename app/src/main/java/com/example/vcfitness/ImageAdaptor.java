package com.example.vcfitness;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;


//class used to display gallery images in the gallery activity

public class ImageAdaptor extends RecyclerView.Adapter<ImageAdaptor.ImageViewHolder> {
    private Context mContext;
    private List<String> mString;

    public ImageAdaptor(Context context, List<String> lstString){
        mContext = context;
        mString = lstString;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.image_item, parent, false);
        return new ImageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
            String strUrl = mString.get(position);
       Picasso.with(mContext).load(strUrl).fit().centerCrop().into(holder.imageview);
    }

    @Override
    public int getItemCount() {
        return mString.size();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder{

        public ImageView imageview;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);

            imageview = itemView.findViewById(R.id.image_view_upload);

        }
    }

}
