/*
 * This is the source code of DMPLayer for Android v. 1.0.0.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Copyright @Dibakar_Mistry, 2015.
 */
package com.dmplayer.childfragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.dmplayer.R;
import com.dmplayer.activities.AlbumAndArtistDetailsActivity;
import com.dmplayer.activities.DMPlayerBaseActivity;
import com.dmplayer.activities.PlaylistDetailsActivity;
import com.dmplayer.manager.MediaController;
import com.dmplayer.models.SongDetail;
import com.dmplayer.phonemidea.DMPlayerUtility;
import com.dmplayer.phonemidea.PhoneMediaControl;
import com.dmplayer.phonemidea.PhoneMediaControl.PhoneMediaControlINterface;
import com.dmplayer.playlist.Playlist;
import com.dmplayer.utility.LogWriter;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;

public class ChildFragmentPlaylists extends Fragment {

    private static final String TAG = "ChildFragmentPlaylists";
    private PlaylistsAdapter mPlaylistsAdapter;
    private ArrayList<Playlist> playlists = new ArrayList<>();
    private static Context context;

    public static ChildFragmentPlaylists newInstance(int position, Context mContext) {
        ChildFragmentPlaylists f = new ChildFragmentPlaylists();
        context = mContext;
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragmentchild_playlists, null);
        setupInitialViews(v);
        loadAllSongs();
        return v;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void setupInitialViews(View inflaterView) {
        RecyclerView recyclerView = (RecyclerView) inflaterView.findViewById(R.id.recyclerview_grid);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        mPlaylistsAdapter = new PlaylistsAdapter(getActivity());
        recyclerView.setAdapter(mPlaylistsAdapter);
    }

    private void loadAllSongs() {

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            ArrayList<File> playlistsPaths = new ArrayList<>();

            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    File parentDir = new File(
                            Environment.getExternalStorageDirectory() + "/DMPlayer/",
                            "DMPlayer_playlists");
                    if (parentDir.exists()) {
                        for (File file : parentDir.listFiles()) {
                            if (file.getName().endsWith(".dpl"))
                                playlistsPaths.add(file);
                        }
                        for(File file : playlistsPaths){
                            ObjectInputStream fin = new ObjectInputStream(new FileInputStream
                                    (file));
                            Playlist current = (Playlist)fin.readObject();
                            current.setPath(file.getPath());
                            playlists.add(current);
                        }
                    }
                    return null;
                } catch (Exception e) {
                    e.printStackTrace();
                    LogWriter.info(TAG, e.toString());
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                mPlaylistsAdapter.notifyDataSetChanged();
            }
        };
        task.execute();

    }

    public class PlaylistsAdapter extends RecyclerView.Adapter<PlaylistsAdapter.ViewHolder> {
        private Context context = null;
        private LayoutInflater layoutInflater;
        private DisplayImageOptions options;
        private ImageLoader imageLoader = ImageLoader.getInstance();

        public PlaylistsAdapter(Context mContext) {
            this.context = mContext;
            this.layoutInflater = LayoutInflater.from(mContext);
            this.options = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.bg_default_album_art)
                    .showImageForEmptyUri(R.drawable.bg_default_album_art).showImageOnFail(R.drawable.bg_default_album_art).cacheInMemory(true)
                    .cacheOnDisk(true).considerExifParams(true).bitmapConfig(Bitmap.Config.RGB_565).build();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.inflate_grid_item, parent, false));
        }

        protected class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            TextView albumName;
            TextView artistName;
            ImageView icon;

            public ViewHolder(View itemView) {
                super(itemView);
                albumName = (TextView) itemView.findViewById(R.id.line1);
                artistName = (TextView) itemView.findViewById(R.id.line2);
                icon = (ImageView) itemView.findViewById(R.id.icon);
                icon.setScaleType(ImageView.ScaleType.CENTER_CROP);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                try {
                    Intent mIntent = new Intent(context, PlaylistDetailsActivity.class);
                    Bundle mBundle = new Bundle();
                    Playlist current = playlists.get(getAdapterPosition());
                    mBundle.putByteArray("playlist", current.getBytes());
                    mIntent.putExtras(mBundle);
                    context.startActivity(mIntent);
                    ((Activity) context).overridePendingTransition(0, 0);
                } catch (Exception e) {
                    e.printStackTrace();
                    LogWriter.info(TAG, e.toString());
                }
            }
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Playlist playlist = playlists.get(position);
            holder.albumName.setText(playlist.getName());
            holder.artistName.setText("Songs: " + String.valueOf(playlist.getSongCount()));
            imageLoader.displayImage("", holder.icon, options);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public int getItemCount() {
            return (playlists != null) ? playlists.size() : 0;
        }

    }

}