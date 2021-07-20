package com.example.pdfdemo.pdf;

import android.graphics.Bitmap;
import android.graphics.RectF;
import android.graphics.pdf.PdfRenderer;
import android.util.SizeF;

import com.example.pdfdemo.PDSElement;
import com.example.pdfdemo.pdf.PDFDocument;

import java.util.ArrayList;

public class PDFPage {

    //添加簽名元素
    private final ArrayList<PDSElement> mElements = new ArrayList<>();
    private SizeF mPageSize = null;
    private final PDFDocument pdfDocument;
    private final int pageNumber;

    public PDFPage(int pageNumber, PDFDocument pdfDocument) {
        this.pageNumber = pageNumber;
        this.pdfDocument = pdfDocument;
    }

    public SizeF getPageSize() {
        if (mPageSize == null) {
            synchronized (PDFDocument.getLockObject()) {
                synchronized (pdfDocument) {
                    //頁面大小
                    PdfRenderer.Page openPage = pdfDocument.getRenderer().openPage(pageNumber);
                    mPageSize = new SizeF(openPage.getWidth(), openPage.getHeight());
                    openPage.close();
                }
            }
        }
        return mPageSize;
    }

    public void renderPage(Bitmap bitmap) {
        synchronized (PDFDocument.getLockObject()) {
            synchronized (pdfDocument) {
                //頁面大小
                PdfRenderer.Page openPage = pdfDocument.getRenderer().openPage(pageNumber);
                mPageSize = new SizeF(openPage.getWidth(), openPage.getHeight());
                openPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                openPage.close();
            }
        }
    }

    public void removeElement(PDSElement fASElement) {
        mElements.remove(fASElement);
    }

    public void addElement(PDSElement fASElement) {
        mElements.add(fASElement);
    }

    public int getNumElements() {
        return mElements.size();
    }

    public PDSElement getElement(int i) {
        return (PDSElement) mElements.get(i);
    }

    public void updateElement(PDSElement fASElement, RectF rectF, float f, float f2, float f3, float f4) {
        fASElement.setRect(rectF);
        if (!(f == 0.0f || f == fASElement.getSize())) {
            fASElement.setSize(f);
        }
        if (!(f2 == 0.0f || f2 == fASElement.getMaxWidth())) {
            fASElement.setMaxWidth(f2);
        }
        if (!(f3 == 0.0f || f3 == fASElement.getStrokeWidth())) {
            fASElement.setStrokeWidth(f3);
        }
        if (!(f4 == 0.0f || f4 == fASElement.getLetterSpace())) {
            fASElement.setLetterSpace(f4);
        }
    }
}
