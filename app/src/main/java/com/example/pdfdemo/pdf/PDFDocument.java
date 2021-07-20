package com.example.pdfdemo.pdf;

import android.content.Context;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

public class PDFDocument {
    private final int mNumPages;
    private static final transient Object sLockObject = new Object();
    private final HashMap<Integer, PDFPage> mPages = new HashMap<>();
    private final transient PdfRenderer mRenderer;
    private final InputStream stream;


    //創建PDF文件
    public PDFDocument(Context context, Uri document) throws IOException {
        stream = context.getContentResolver().openInputStream(document);

        synchronized (sLockObject) {
            mRenderer = new PdfRenderer(context.getContentResolver().openFileDescriptor(document, "r"));
            mNumPages = this.mRenderer.getPageCount();
        }
    }

    //用來記錄獲取第幾頁
    public PDFPage getPage(int i) {
        if (i >= mNumPages || i < 0) {
            return null;
        }
        PDFPage fASPDFPage = (PDFPage) mPages.get(i);
        if (fASPDFPage != null) {
            return fASPDFPage;
        }
        PDFPage fASPDFPage2 = new PDFPage(i, this);
        mPages.put(i, fASPDFPage2);
        return fASPDFPage2;
    }

    //同步鎖
    public static Object getLockObject() {
        return sLockObject;
    }

    //獲取PDF文件
    public PdfRenderer getRenderer() {
        return mRenderer;
    }

    //獲取PDF總頁數
    public int getNumPages() {
        return mNumPages;
    }

    //獲取PDF流
    public InputStream getStream() {
        return stream;
    }
}
