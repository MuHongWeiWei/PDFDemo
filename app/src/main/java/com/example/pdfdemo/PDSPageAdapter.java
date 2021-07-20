package com.example.pdfdemo;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;


import com.example.pdfdemo.pdf.PDFDocument;

import org.jetbrains.annotations.NotNull;


public class PDSPageAdapter extends FragmentStatePagerAdapter {

    private final PDFDocument mDocument;

    public PDSPageAdapter(FragmentManager fragmentManager, PDFDocument mDocument) {
        super(fragmentManager, FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.mDocument = mDocument;
    }

    public int getCount() {
        return mDocument.getNumPages();
    }

    @NotNull
    public Fragment getItem(int i) {
        return PDSFragment.newInstance(i);
    }

}
