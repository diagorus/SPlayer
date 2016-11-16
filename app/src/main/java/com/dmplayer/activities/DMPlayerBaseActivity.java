package com.dmplayer.activities;

import android.annotation.TargetApi;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dmplayer.R;
import com.dmplayer.adapter.DrawerAdapter;
import com.dmplayer.fragments.FragmentChat;
import com.dmplayer.fragments.FragmentEqualizer;
import com.dmplayer.fragments.FragmentFavorite;
import com.dmplayer.fragments.FragmentLibrary;
import com.dmplayer.fragments.FragmentSettings;
import com.dmplayer.fragments.FragmentStream;
import com.dmplayer.manager.MediaController;
import com.dmplayer.manager.MusicPreferance;
import com.dmplayer.manager.NotificationManager;
import com.dmplayer.models.DrawerItem;
import com.dmplayer.models.SongDetail;
import com.dmplayer.phonemedia.DMPlayerUtility;
import com.dmplayer.recyclerviewutils.ItemClickSupport;
import com.dmplayer.slidinguppanelhelper.SlidingUpPanelLayout;
import com.dmplayer.uicomponent.CircleImageView;
import com.dmplayer.uicomponent.PlayPauseView;
import com.dmplayer.uicomponent.Slider;
import com.dmplayer.utility.AssetsCopier;
import com.dmplayer.utility.LogWriter;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


public class DMPlayerBaseActivity extends AppCompatActivity implements View.OnClickListener,
        Slider.OnValueChangedListener,
        NotificationManager.NotificationCenterDelegate,
        SensorEventListener {

    private static final String TAG = DMPlayerBaseActivity.class.getSimpleName();
    private Context context;
    private SharedPreferences sharedPreferences;
    private ActionBarDrawerToggle mDrawerToggle;

    private Toolbar toolbar;
    private DrawerLayout mDrawerLayout;

    private RecyclerView recyclerViewDrawer;
    private RecyclerView.Adapter adapterDrawer;

    private SlidingUpPanelLayout mLayout;
    private RelativeLayout slidepanelchildtwo_topviewone;
    private RelativeLayout slidepanelchildtwo_topviewtwo;
    private boolean isExpand = false;

    private DisplayImageOptions options;
    private ImageLoader imageLoader = ImageLoader.getInstance();
    private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();

    private ImageView songBackground;
    private ImageView img_bottom_slideone;
    private ImageView img_bottom_slidetwo;
    private TextView txt_playesongname;
    private TextView txt_songartistname;
    private TextView txt_playesongname_slidetoptwo;
    private TextView txt_songartistname_slidetoptwo;
    private TextView txt_timeprogress;
    private TextView txt_timetotal;
    private ImageView imgbtn_backward;
    private ImageView imgbtn_forward;
    private ImageView imgbtn_toggle;
    private ImageView imgbtn_suffel;
    private ImageView img_Favorite;
    private PlayPauseView btn_playpause;
    private PlayPauseView btn_playpausePanel;
    private Slider audio_progress;

    private boolean isDraggingStart = false;
    private int TAG_Observer;

    private SensorManager senSensorManager;
    private Sensor senAccelerometer;

    private long lastUpdate = 0;
    private float last_x, last_y, last_z;
    private static final int SHAKE_THRESHOLD = 600;

    String MIXING_MODE = "mixing_mode";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        Set your theme first
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        context = DMPlayerBaseActivity.this;
        sharedPreferences = getSharedPreferences("VALUES", Context.MODE_PRIVATE);

        theme();


        new AssetsCopier(this).execute();

        //Set your Layout view
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dmplayerbase);

        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer , SensorManager.SENSOR_DELAY_NORMAL);

        toolbarStatusBar();
        navigationDrawer();



        initSlidingUpPanel();

        header();

        setFragment(0);

        getIntentData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        addObserver();
        try {
            loadAlreadyPlaying();
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        removeObserver();
    }

    @Override
    protected void onDestroy() {
        removeObserver();
        if (MediaController.getInstance().isAudioPaused()) {
            MediaController.getInstance().cleanupPlayer(context, true, true);
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (isExpand) {
            mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        } else {
            int count = getFragmentManager().getBackStackEntryCount();
            if (count == 0) {
                super.onBackPressed();
                overridePendingTransition(0, 0);
                finish();
            } else {
                getFragmentManager().popBackStack();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bottombar_play:
            case R.id.btn_play:
                if (MediaController.getInstance().getPlayingSongDetail() != null)
                    PlayPauseEvent(v);
                break;

            case R.id.btn_forward:
                if (MediaController.getInstance().getPlayingSongDetail() != null)
                    MediaController.getInstance().playNextSong();
                break;

            case R.id.btn_backward:
                if (MediaController.getInstance().getPlayingSongDetail() != null)
                    MediaController.getInstance().playPreviousSong();
                break;

            case R.id.btn_suffel:
                break;

            case R.id.btn_toggle:
                break;

            case R.id.bottombar_img_Favorite:
                if (MediaController.getInstance().getPlayingSongDetail() != null) {
                    MediaController.getInstance().storeFavoritePlay(context, MediaController.getInstance().getPlayingSongDetail(), v.isSelected() ? 0 : 1);
                    v.setSelected(!v.isSelected());
                    DMPlayerUtility.animateHeartButton(v);
                    findViewById(R.id.like).setSelected(!v.isSelected());
                    DMPlayerUtility.animatePhotoLike(findViewById(R.id.big_like), findViewById(R.id.like));
                }
                break;

            default:
                break;
        }
    }

    /**
     * Get intent data from music choose option
     */
    void getIntentData() {
        try {
            Uri data = getIntent().getData();
            if (data != null) {
                if (data.getScheme().equalsIgnoreCase("file")) {
                    String path = data.getPath();
                    if (!TextUtils.isEmpty(path)) {
                        MediaController.getInstance().cleanupPlayer(context, true, true);
                        MusicPreferance.getPlaylist(context, path);
                        updateTitle(false);
                        MediaController.getInstance().playAudio(MusicPreferance.playingSongDetail);
                        mLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                    }
                }
                if (data.getScheme().equalsIgnoreCase("http"))
                    LogWriter.info(TAG, data.getPath());
                if (data.getScheme().equalsIgnoreCase("content"))
                    LogWriter.info(TAG, data.getPath());

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void toolbarStatusBar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    void setDrawersRightMargin() {
        // Cast drawer
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);

        View drawer = findViewById(R.id.scrimInsetsFrameLayout);
        ViewGroup.LayoutParams layoutParams = drawer.getLayoutParams();
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            layoutParams.width = displayMetrics.widthPixels - (70 * Math.round(displayMetrics.density));
        }
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            layoutParams.width = displayMetrics.widthPixels + (25 * Math.round(displayMetrics.density)) - displayMetrics.widthPixels / 2;
        }
    }

    void setupDrawerIcon() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        mDrawerToggle.syncState();
    }

    void setStatusBarBehindDrawer() {
        TypedValue typedValueStatusBarColor = new TypedValue();
        getTheme().resolveAttribute(R.attr.colorPrimaryDark, typedValueStatusBarColor, true);
        final int colorStatusBar = typedValueStatusBarColor.data;
        mDrawerLayout.setStatusBarBackgroundColor(colorStatusBar);
    }

    public void navigationDrawer() {
        setDrawersRightMargin();

        setupDrawerIcon();

        // statusBar color behind navigation drawer
        setStatusBarBehindDrawer();

        // Setup RecyclerView inside drawer
        final TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
        final int color = typedValue.data;

        recyclerViewDrawer = (RecyclerView) findViewById(R.id.recyclerViewDrawer);
        recyclerViewDrawer.setHasFixedSize(true);
        recyclerViewDrawer.setLayoutManager(new LinearLayoutManager(DMPlayerBaseActivity.this));

        ArrayList<DrawerItem> drawerItems = new ArrayList<>();
        final String[] drawerTitles = getResources().getStringArray(R.array.drawer);
        final TypedArray drawerIcons = getResources().obtainTypedArray(R.array.drawer_icons);
        for (int i = 0; i < drawerTitles.length; i++) {
            drawerItems.add(new DrawerItem(drawerTitles[i], drawerIcons.getDrawable(i)));
        }
        drawerIcons.recycle();
        adapterDrawer = new DrawerAdapter(drawerItems);
        recyclerViewDrawer.setAdapter(adapterDrawer);

        recyclerViewDrawer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onGlobalLayout() {

                for (int i = 0; i < drawerTitles.length; i++) {
                    if (i == sharedPreferences.getInt("FRAGMENT", 0)) {
                        ImageView imageViewDrawerIcon = (ImageView) recyclerViewDrawer.getChildAt(i).findViewById(R.id.imageViewDrawerIcon);
                        TextView textViewDrawerTitle = (TextView) recyclerViewDrawer.getChildAt(i).findViewById(R.id.textViewDrawerItemTitle);
                        imageViewDrawerIcon.setColorFilter(color);
                        imageViewDrawerIcon.setImageAlpha(255);
                        textViewDrawerTitle.setTextColor(color);
                        RelativeLayout relativeLayoutDrawerItem = (RelativeLayout) recyclerViewDrawer.getChildAt(i).findViewById(R.id.relativeLayoutDrawerItem);
                        TypedValue typedValueDrawerSelected = new TypedValue();
                        getTheme().resolveAttribute(R.attr.colorPrimary, typedValueDrawerSelected, true);
                        int colorDrawerItemSelected = typedValueDrawerSelected.data;
                        colorDrawerItemSelected = (colorDrawerItemSelected & 0x00FFFFFF) | 0x30000000;
                        relativeLayoutDrawerItem.setBackgroundColor(colorDrawerItemSelected);
                    } else {
                        ImageView imageViewDrawerIcon = (ImageView) recyclerViewDrawer.getChildAt(i).findViewById(R.id.imageViewDrawerIcon);
                        TextView textViewDrawerTitle = (TextView) recyclerViewDrawer.getChildAt(i).findViewById(R.id.textViewDrawerItemTitle);
                        imageViewDrawerIcon.setColorFilter(getResources().getColor(R.color.md_text));
                        imageViewDrawerIcon.setImageAlpha(138);

                        textViewDrawerTitle.setTextColor(getResources().getColor(R.color.md_text));
                        RelativeLayout relativeLayoutDrawerItem = (RelativeLayout) recyclerViewDrawer.getChildAt(i).findViewById(R.id.relativeLayoutDrawerItem);
                        relativeLayoutDrawerItem.setBackgroundColor(getResources().getColor(R.color.md_white_1000));
                    }
                }

                // unregister listener (this is important)
                recyclerViewDrawer.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });

        // RecyclerView item listener.
        ItemClickSupport itemClickSupport = ItemClickSupport.addTo(recyclerViewDrawer);
        itemClickSupport.setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClick(RecyclerView parent, View view, final int position, long id) {

                for (int i = 0; i < drawerTitles.length; i++) {
                    if (i == position) {
                        ImageView imageViewDrawerIcon = (ImageView) recyclerViewDrawer.getChildAt(i).findViewById(R.id.imageViewDrawerIcon);
                        TextView textViewDrawerTitle = (TextView) recyclerViewDrawer.getChildAt(i).findViewById(R.id.textViewDrawerItemTitle);
                        imageViewDrawerIcon.setColorFilter(color);
                        imageViewDrawerIcon.setImageAlpha(255);
                        textViewDrawerTitle.setTextColor(color);
                        RelativeLayout relativeLayoutDrawerItem = (RelativeLayout) recyclerViewDrawer.getChildAt(i).findViewById(R.id.relativeLayoutDrawerItem);
                        TypedValue typedValueDrawerSelected = new TypedValue();
                        getTheme().resolveAttribute(R.attr.colorPrimary, typedValueDrawerSelected, true);
                        int colorDrawerItemSelected = typedValueDrawerSelected.data;
                        colorDrawerItemSelected = (colorDrawerItemSelected & 0x00FFFFFF) | 0x30000000;
                        relativeLayoutDrawerItem.setBackgroundColor(colorDrawerItemSelected);

                    } else {
                        ImageView imageViewDrawerIcon = (ImageView) recyclerViewDrawer.getChildAt(i).findViewById(R.id.imageViewDrawerIcon);
                        TextView textViewDrawerTitle = (TextView) recyclerViewDrawer.getChildAt(i).findViewById(R.id.textViewDrawerItemTitle);
                        imageViewDrawerIcon.setColorFilter(getResources().getColor(R.color.md_text));
                        imageViewDrawerIcon.setImageAlpha(138);
                        textViewDrawerTitle.setTextColor(getResources().getColor(R.color.md_text));
                        RelativeLayout relativeLayoutDrawerItem = (RelativeLayout) recyclerViewDrawer.getChildAt(i).findViewById(R.id.relativeLayoutDrawerItem);
                        relativeLayoutDrawerItem.setBackgroundColor(getResources().getColor(R.color.md_white_1000));
                    }
                }

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // Do something after some time
                        setFragment(position);
                        if (isExpand) {
                            mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                        }
                    }
                }, 250);
                mDrawerLayout.closeDrawers();
            }
        });
    }

    private void initSlidingUpPanel() {
        mLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        songBackground = (ImageView) findViewById(R.id.image_songsAlbum);
        img_bottom_slideone = (ImageView) findViewById(R.id.img_bottom_slideone);
        img_bottom_slidetwo = (ImageView) findViewById(R.id.img_bottom_slidetwo);

        txt_timeprogress = (TextView) findViewById(R.id.slidepanel_time_progress);
        txt_timetotal = (TextView) findViewById(R.id.slidepanel_time_total);
        imgbtn_backward = (ImageView) findViewById(R.id.btn_backward);
        imgbtn_forward = (ImageView) findViewById(R.id.btn_forward);
        imgbtn_toggle = (ImageView) findViewById(R.id.btn_toggle);
        imgbtn_suffel = (ImageView) findViewById(R.id.btn_suffel);
        btn_playpause = (PlayPauseView) findViewById(R.id.btn_play);
        audio_progress = (Slider) findViewById(R.id.audio_progress_control);
        btn_playpausePanel = (PlayPauseView) findViewById(R.id.bottombar_play);
        img_Favorite = (ImageView) findViewById(R.id.bottombar_img_Favorite);

        //TODO: Attention! Resolving attributes
        TypedValue typedvaluecoloraccent = new TypedValue();
        getTheme().resolveAttribute(R.attr.colorAccent, typedvaluecoloraccent, true);
        final int coloraccent = typedvaluecoloraccent.data;

        audio_progress.setBackgroundColor(coloraccent);
        audio_progress.setValue(0);

        audio_progress.setOnValueChangedListener(this);
        imgbtn_backward.setOnClickListener(this);
        imgbtn_forward.setOnClickListener(this);
        imgbtn_toggle.setOnClickListener(this);
        imgbtn_suffel.setOnClickListener(this);
        img_Favorite.setOnClickListener(this);

        btn_playpausePanel.Pause();
        btn_playpause.Pause();

        txt_playesongname = (TextView) findViewById(R.id.txt_playesongname);
        txt_songartistname = (TextView) findViewById(R.id.txt_songartistname);
        txt_playesongname_slidetoptwo = (TextView) findViewById(R.id.txt_playesongname_slidetoptwo);
        txt_songartistname_slidetoptwo = (TextView) findViewById(R.id.txt_songartistname_slidetoptwo);

        slidepanelchildtwo_topviewone = (RelativeLayout) findViewById(R.id.slidepanelchildtwo_topviewone);
        slidepanelchildtwo_topviewtwo = (RelativeLayout) findViewById(R.id.slidepanelchildtwo_topviewtwo);

        slidepanelchildtwo_topviewone.setVisibility(View.VISIBLE);
        slidepanelchildtwo_topviewtwo.setVisibility(View.INVISIBLE);

        slidepanelchildtwo_topviewone.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);

            }
        });

        slidepanelchildtwo_topviewtwo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);

            }
        });

        ((PlayPauseView) findViewById(R.id.bottombar_play)).setOnClickListener(this);
        ((PlayPauseView) findViewById(R.id.btn_play)).setOnClickListener(this);

        mLayout.setPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                Log.i(TAG, "onPanelSlide, offset " + slideOffset);

                if (slideOffset == 0.0f) {
                    isExpand = false;
                    slidepanelchildtwo_topviewone.setVisibility(View.VISIBLE);
                    slidepanelchildtwo_topviewtwo.setVisibility(View.INVISIBLE);
                } else if (slideOffset > 0.0f && slideOffset < 1.0f) {
                    // if (isExpand) {
                    // slidepanelchildtwo_topviewone.setAlpha(1.0f);
                    // slidepanelchildtwo_topviewtwo.setAlpha(1.0f -
                    // slideOffset);
                    // } else {
                    // slidepanelchildtwo_topviewone.setAlpha(1.0f -
                    // slideOffset);
                    // slidepanelchildtwo_topviewtwo.setAlpha(1.0f);
                    // }

                } else {
                    isExpand = true;
                    slidepanelchildtwo_topviewone.setVisibility(View.INVISIBLE);
                    slidepanelchildtwo_topviewtwo.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onPanelExpanded(View panel) {
                Log.i(TAG, "onPanelExpanded");
                isExpand = true;
            }

            @Override
            public void onPanelCollapsed(View panel) {
                Log.i(TAG, "onPanelCollapsed");
                isExpand = false;
            }

            @Override
            public void onPanelAnchored(View panel) {
                Log.i(TAG, "onPanelAnchored");
            }

            @Override
            public void onPanelHidden(View panel) {
                Log.i(TAG, "onPanelHidden");
            }
        });

        header();
    }

    public void setFragment(int position) {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        switch (position) {
            case 0:
                sharedPreferences.edit().putInt("FRAGMENT", position).apply();
                FragmentLibrary testfragmentlibrary = new FragmentLibrary();
                fragmentTransaction.replace(R.id.fragment, testfragmentlibrary);
                fragmentTransaction.commit();
                toolbar.setTitle("My Library");
                break;

            case 1:
                sharedPreferences.edit().putInt("FRAGMENT", position).apply();
                FragmentFavorite fragmentfavorite = new FragmentFavorite();
                fragmentTransaction.replace(R.id.fragment, fragmentfavorite);
                fragmentTransaction.commit();
                toolbar.setTitle("Favorite");
                break;

            case 2:
                sharedPreferences.edit().putInt("FRAGMENT", position).apply();
                FragmentStream fragmentStream = new FragmentStream();
                fragmentTransaction.replace(R.id.fragment, fragmentStream);
                fragmentTransaction.commit();
                toolbar.setTitle("Stream");
                break;

            case 3:
                sharedPreferences.edit().putInt("FRAGMENT", position).apply();
                FragmentChat fragmentChat = new FragmentChat();
                fragmentTransaction.replace(R.id.fragment,fragmentChat);
                fragmentTransaction.commit();
                toolbar.setTitle("Chat");
                break;

            case 4:
                sharedPreferences.edit().putInt("FRAGMENT", position).apply();
                FragmentEqualizer fragmentequalizer = new FragmentEqualizer();
                fragmentTransaction.replace(R.id.fragment, fragmentequalizer);
                fragmentTransaction.commit();
                toolbar.setTitle("Equilizer");
                break;

            case 5:
                sharedPreferences.edit().putInt("FRAGMENT", position).apply();
                FragmentSettings fragmentsettings = new FragmentSettings();
                fragmentTransaction.replace(R.id.fragment, fragmentsettings);
                fragmentTransaction.commit();
                toolbar.setTitle("Settings");
                break;
        }
    }

    public void theme() {
        int theme = sharedPreferences.getInt("THEME", 0);

        DMPlayerUtility.settingTheme(context, theme);
    }

    public void header() {
        setBackgroundImage();
        setAvatarImage();
        setUserName();
    }

    private void setBackgroundImage() {
        ImageView headerBackgroundImage = (ImageView) findViewById(R.id.imageViewCover);

        String headerBackground = sharedPreferences.getString(FragmentSettings.HEADER_BACKGROUND, "");
        Uri headerBackgroundUri = Uri.parse(headerBackground);

        if (DMPlayerUtility.isURIExists(headerBackgroundUri)) {
            DMPlayerUtility.settingPicture(headerBackgroundImage, headerBackgroundUri);
        } else {
            DMPlayerUtility.settingPicture(headerBackgroundImage, R.drawable.drawer_defult_header);
        }
    }

    private void setAvatarImage() {
        CircleImageView avatarImage = (CircleImageView) findViewById(R.id.profileAvatar);

        String avatar = sharedPreferences.getString(FragmentSettings.AVATAR, "");
        Uri avatarUri = Uri.parse(avatar);

        if (DMPlayerUtility.isURIExists(avatarUri)) {
            DMPlayerUtility.settingPicture(avatarImage, avatarUri);
        } else {
            DMPlayerUtility.settingPicture(avatarImage, R.drawable.avatar_default);
        }
    }

    private void setUserName() {
        TextView nameText = (TextView) findViewById(R.id.profileName);

        String name = sharedPreferences.getString(FragmentSettings.NAME, "");

        if (!name.equals("")) {
            nameText.setText(name);
        } else {
            nameText.setText(R.string.profile_defult_name);
        }
    }

    private void loadImageLoaderOption() {
        this.options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.bg_default_album_art)
                .showImageForEmptyUri(R.drawable.bg_default_album_art)
                .showImageOnFail(R.drawable.bg_default_album_art)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
    }

    private void loadAlreadyPlaying() {
        SongDetail mSongDetail = MusicPreferance.getLastSong(context);
        List<SongDetail> playlist = MusicPreferance.getPlaylist(context);
        if (mSongDetail != null) {
            updateTitle(false);
        }
        MediaController.getInstance().checkIsFavorite(context, mSongDetail, img_Favorite);
    }

    public void addObserver() {
        TAG_Observer = MediaController.getInstance().generateObserverTag();
        NotificationManager.getInstance().addObserver(this, NotificationManager.audioDidReset);
        NotificationManager.getInstance().addObserver(this, NotificationManager.audioPlayStateChanged);
        NotificationManager.getInstance().addObserver(this, NotificationManager.audioDidStarted);
        NotificationManager.getInstance().addObserver(this, NotificationManager.audioProgressDidChanged);
        NotificationManager.getInstance().addObserver(this, NotificationManager.newaudioloaded);
    }

    public void removeObserver() {
        NotificationManager.getInstance().removeObserver(this, NotificationManager.audioDidReset);
        NotificationManager.getInstance().removeObserver(this, NotificationManager.audioPlayStateChanged);
        NotificationManager.getInstance().removeObserver(this, NotificationManager.audioDidStarted);
        NotificationManager.getInstance().removeObserver(this, NotificationManager.audioProgressDidChanged);
        NotificationManager.getInstance().removeObserver(this, NotificationManager.newaudioloaded);
    }

    public void loadSongsDetails(SongDetail mDetail) {
        String contentURI = "content://media/external/audio/media/" + mDetail.getId() + "/albumart";
        imageLoader.displayImage(contentURI, songBackground, options, animateFirstListener);
        imageLoader.displayImage(contentURI, img_bottom_slideone, options, animateFirstListener);
        imageLoader.displayImage(contentURI, img_bottom_slidetwo, options, animateFirstListener);

        txt_playesongname.setText(mDetail.getTitle());
        txt_songartistname.setText(mDetail.getArtist());
        txt_playesongname_slidetoptwo.setText(mDetail.getTitle());
        txt_songartistname_slidetoptwo.setText(mDetail.getArtist());

        if (txt_timetotal != null) {
            long duration = Long.valueOf(mDetail.getDuration());
            txt_timetotal.setText(duration != 0 ? String.format("%d:%02d", duration / 60, duration % 60) : "-:--");
        }
        updateProgress(mDetail);
    }

    @Override
    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationManager.audioDidStarted || id == NotificationManager.audioPlayStateChanged || id == NotificationManager.audioDidReset) {
            updateTitle(id == NotificationManager.audioDidReset && (Boolean) args[1]);
        } else if (id == NotificationManager.audioProgressDidChanged) {
            SongDetail mSongDetail = MediaController.getInstance().getPlayingSongDetail();
            updateProgress(mSongDetail);
        }
    }

    @Override
    public void newSongLoaded(Object... args) {
        MediaController.getInstance().checkIsFavorite(context, (SongDetail) args[0], img_Favorite);
    }

    private void updateTitle(boolean shutdown) {
        SongDetail mSongDetail = MediaController.getInstance().getPlayingSongDetail();
        if (mSongDetail != null && !shutdown) {
            updateProgress(mSongDetail);
            if (MediaController.getInstance().isAudioPaused()) {
                btn_playpausePanel.Pause();
                btn_playpause.Pause();
            } else {
                btn_playpausePanel.Play();
                btn_playpause.Play();
            }
            SongDetail audioInfo = MediaController.getInstance().getPlayingSongDetail();
            loadSongsDetails(audioInfo);

            if (txt_timetotal != null) {
                long duration = Long.valueOf(audioInfo.getDuration());
                txt_timetotal.setText(duration != 0 ? String.format("%d:%02d", duration / 60, duration % 60) : "-:--");
            }
        }
    }

    private void updateProgress(SongDetail mSongDetail) {
        if (audio_progress != null) {
            // When SeekBar Draging Don't Show Progress
            if (!isDraggingStart) {
                // Progress Value comming in point it range 0 to 1
                audio_progress.setValue((int) (mSongDetail.audioProgress * 100));
            }
            String timeString = String.format("%d:%02d", mSongDetail.audioProgressSec / 60, mSongDetail.audioProgressSec % 60);
            txt_timeprogress.setText(timeString);
        }
    }

    private void PlayPauseEvent(View v) {
        if (MediaController.getInstance().isAudioPaused()) {
            MediaController.getInstance().playAudio(MediaController.getInstance().getPlayingSongDetail());
            ((PlayPauseView) v).Play();
        } else {
            MediaController.getInstance().pauseAudio(MediaController.getInstance().getPlayingSongDetail());
            ((PlayPauseView) v).Pause();
        }
    }

    @Override
    public void onValueChanged(int value) {
        MediaController.getInstance().seekToProgress(MediaController.getInstance().getPlayingSongDetail(), (float) value / 100);
    }

    private void setTranslucentStatus(boolean on) {
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }




    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;

        if(getMixingMode().equals("ON")){
            if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                float x = sensorEvent.values[0];
                float y = sensorEvent.values[1];
                float z = sensorEvent.values[2];
                long curTime = System.currentTimeMillis();
                if ((curTime - lastUpdate) > 100) {
                    long diffTime = (curTime - lastUpdate);
                    lastUpdate = curTime;
                    float speed = Math.abs(x + y + z - last_x - last_y - last_z)/ diffTime * 10000;
                    if (speed > SHAKE_THRESHOLD) {
                        //  getRandomNumber();

                        if (MediaController.getInstance().getPlayingSongDetail() != null)
                            MediaController.getInstance().playNextSong();
                    }
                    last_x = x;
                    last_y = y;
                    last_z = z;
                }
            }
        }

    }
    private String getMixingMode() {
        return sharedPreferences.getString(MIXING_MODE, "");
    }
    private static class AnimateFirstDisplayListener extends SimpleImageLoadingListener {
        static final List<String> displayedImages = Collections.synchronizedList(new LinkedList<String>());

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            if (loadedImage != null) {
                ImageView imageView = (ImageView) view;
                boolean firstDisplay = !displayedImages.contains(imageUri);
                if (firstDisplay) {
                    FadeInBitmapDisplayer.animate(imageView, 500);
                    displayedImages.add(imageUri);
                }
            }
        }

    }

}
