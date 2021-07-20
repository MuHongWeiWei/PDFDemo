package com.example.pdfdemo;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;


import com.example.pdfdemo.pdf.PDFDocument;
import com.example.pdfdemo.pdf.PDFPage;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class PDSSaveAsPDFAsyncTask extends AsyncTask<Void, Void, Boolean> {


    @SuppressLint("StaticFieldLeak")
    private final DigitalSignatureActivity mCtx;


    public PDSSaveAsPDFAsyncTask(DigitalSignatureActivity context) {
        this.mCtx = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    public Boolean doInBackground(Void... voidArr) {
        if (Looper.myLooper() == null) {
            Looper.prepare();
        }
        PDFDocument document = mCtx.getDocument();
        File root = mCtx.getFilesDir();

        File myDir = new File(root + "/DigitalSignature");
        if (!myDir.exists()) {
            myDir.mkdirs();
        }
        File file = new File(myDir.getAbsolutePath(), "test.pdf");
        if (file.exists())
            file.delete();
        try {
            InputStream stream = document.getStream();
            FileOutputStream os = new FileOutputStream(file);
            PdfReader reader = new PdfReader(stream);
            PdfStamper signer = null;
            Bitmap createBitmap = null;
            for (int i = 0; i < document.getNumPages(); i++) {
                Rectangle mediabox = reader.getPageSize(i + 1);
                for (int j = 0; j < document.getPage(i).getNumElements(); j++) {
                    PDFPage page = document.getPage(i);
                    PDSElement element = page.getElement(j);
                    RectF bounds = element.getRect();
                    if (element.getType() == PDSElement.PDSElementType.PDSElementTypeSignature) {
                        PDSElementViewer viewer = element.mElementViewer;
                        View dummy = viewer.getElementView();
                        View view = ViewUtils.createSignatureView(mCtx, element, viewer.mPageViewer.getToViewCoordinatesMatrix());
                        createBitmap = Bitmap.createBitmap(dummy.getWidth(), dummy.getHeight(), Bitmap.Config.ARGB_8888);
                        view.draw(new Canvas(createBitmap));
                    } else {
                        createBitmap = element.getBitmap();
                    }
                    ByteArrayOutputStream saveBitmap = new ByteArrayOutputStream();
                    createBitmap.compress(Bitmap.CompressFormat.PNG, 100, saveBitmap);
                    byte[] byteArray = saveBitmap.toByteArray();
                    createBitmap.recycle();

                    Image sigimage = Image.getInstance(byteArray);
                        if (signer == null)
                            signer = new PdfStamper(reader, os, '\0');
                        PdfContentByte contentByte = signer.getOverContent(i + 1);
                        sigimage.setAlignment(Image.ALIGN_UNDEFINED);
                        sigimage.scaleToFit(bounds.width(), bounds.height());
                        sigimage.setAbsolutePosition(bounds.left - (sigimage.getScaledWidth() - bounds.width()) / 2, mediabox.getHeight() - (bounds.top + bounds.height()));
                        contentByte.addImage(sigimage);
                }
            }
            if (signer != null)
                signer.close();
            reader.close();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
            if (file.exists()) {
                file.delete();
            }
            return false;
        }
        return true;
    }

    @Override
    public void onPostExecute(Boolean result) {
        mCtx.refresh();
        if (!result)
            Toast.makeText(mCtx, "簽名失敗", Toast.LENGTH_LONG).show();
        else
            Toast.makeText(mCtx, "簽名成功", Toast.LENGTH_LONG).show();
    }
}

