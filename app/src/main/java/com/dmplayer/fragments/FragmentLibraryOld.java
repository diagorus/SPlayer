/*
 * This is the source code of DMPLayer for Android v. 1.0.0.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Copyright @Dibakar_Mistry, 2015.
 */
package com.dmplayer.fragments;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dmplayer.R;
import com.dmplayer.childfragment.ChildFragmentAlbums;
import com.dmplayer.childfragment.ChildFragmentAllSongs;
import com.dmplayer.childfragment.ChildFragmentArtists;
import com.dmplayer.childfragment.ChildFragmentGenres;
import com.dmplayer.childfragment.ChildFragmentMostPlayed;
import com.dmplayer.childfragment.ChildFragmentPlaylists;
import com.dmplayer.tablayout.SlidingTabLayout;

public class FragmentLibraryOld extends Fragment {

    private final String[] TITLES = {"ALL SONGS","ALBUMS", "ARTISTS", "GENRES", "MOST PLAYED",
            "PLAYLISTS", "TEST"};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_library_old, null);
        setupView(v);
        return v;
    }

    private void setupView(View view) {
        ViewPager pager = (ViewPager) view.findViewById(R.id.pager);
        MyPagerAdapter pagerAdapter = new MyPagerAdapter(getFragmentManager());
        pager.setAdapter(pagerAdapter);
        SlidingTabLayout tabs = (SlidingTabLayout) view.findViewById(R.id.tabs);
        tabs.setDistributeEvenly(false);
        // Tab indicator color
        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.md_white_1000);
            }
        });
        tabs.setViewPager(pager);
    }

    public class MyPagerAdapter extends FragmentStatePagerAdapter {
        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return TITLES[position];
        }

        @Override
        public int getCount() {
            return TITLES.length;
        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return ChildFragmentAllSongs.newInstance(position, getActivity());

                case 1:
                    return ChildFragmentAlbums.newInstance(position, getActivity());

                case 2:
                    return ChildFragmentArtists.newInstance(position, getActivity());

                case 3:
                    return ChildFragmentGenres.newInstance(position, getActivity());

                case 4:
                    return ChildFragmentMostPlayed.newInstance(position, getActivity());

                case 5:
                    return ChildFragmentPlaylists.newInstance(position);
                case 6:
                    return new FragmentLibrary();
                default:
                    throw new IllegalArgumentException("Fragment out of range");
            }
        }
    }
}
