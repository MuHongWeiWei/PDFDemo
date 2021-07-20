package com.example.pdfdemo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ShareCompat;
import androidx.core.content.FileProvider;

import com.example.pdfdemo.pdf.PDFDocument;

import java.io.File;


public class DigitalSignatureActivity extends AppCompatActivity {

    private static final int SIGNATURE_Request_CODE = 43;
    private PDSViewPager mViewPager;
    private PDFDocument mDocument;
    private Menu menu;
    private File currentFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_digital_signature);
        mViewPager = findViewById(R.id.viewpager);

        String message = getIntent().getStringExtra("ActivityAction");

        //第一次未修改第二次修改過
        if (message.equals("once")) {
            currentFile = new File(getFilesDir(), "test.pdf");
            Uri contentUri = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName() + ".provider", currentFile);
            OpenPDFViewer(contentUri);
        } else if (message.equals("twice")) {
            currentFile = new File(getFilesDir() + "/DigitalSignature", "test.pdf");
            Uri contentUri = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName() + ".provider", currentFile);
            OpenPDFViewer(contentUri);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent result) {
        super.onActivityResult(requestCode, resultCode, result);
        //簽名回來 把簽名印上去
        if (requestCode == SIGNATURE_Request_CODE && resultCode == Activity.RESULT_OK) {
            String returnValue = result.getStringExtra("FileName");
            File fi = new File(returnValue);
            addElement(PDSElement.PDSElementType.PDSElementTypeSignature, fi, (float) SignatureUtils.getSignatureWidth((int) getResources().getDimension(R.dimen.sign_field_default_height), fi, getApplicationContext()), getResources().getDimension(R.dimen.sign_field_default_height));
        }
    }

    //初始化menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        this.menu = menu;
        MenuItem saveItem = menu.findItem(R.id.action_save);
        saveItem.getIcon().setAlpha(130);
        MenuItem signItem = menu.findItem(R.id.action_sign);
        signItem.getIcon().setAlpha(255);
        return true;
    }

    //點擊簽名或儲存
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sign:
                startActivityForResult(new Intent(getApplicationContext(), SignatureActivity.class), SIGNATURE_Request_CODE);
                break;
            case R.id.action_save:
                savePDFDocument();
                break;
            case R.id.action_share:
                Uri contentUri = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName() + ".provider", currentFile);
                Intent intent = ShareCompat.IntentBuilder
                        .from(this)
                        .setStream(contentUri)
                        .getIntent();
                intent.setDataAndType(contentUri, "application/pdf");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(intent);
                break;
        }
        return true;
    }

    private void OpenPDFViewer(Uri pdfData) {
        try {
            mDocument = new PDFDocument(this, pdfData);
            PDSPageAdapter imageAdapter = new PDSPageAdapter(getSupportFragmentManager(), mDocument);
            //預設頁數為1
            updatePageNumber(1);
            mViewPager.setAdapter(imageAdapter);
        } catch (Exception e) {
            Toast.makeText(DigitalSignatureActivity.this, "PDF開啟失敗", Toast.LENGTH_LONG).show();
        }
    }

    public PDFDocument getDocument() {
        return mDocument;
    }

    //判斷有簽名才能儲存
    public void invokeMenuButton(boolean disableButtonFlag) {
        MenuItem saveItem = menu.findItem(R.id.action_save);
        saveItem.setEnabled(disableButtonFlag);
        if (disableButtonFlag) {
            saveItem.getIcon().setAlpha(255);
        } else {
            saveItem.getIcon().setAlpha(130);
        }
    }

    //添加簽名
    public void addElement(PDSElement.PDSElementType fASElementType, File file, float f, float f2) {
        View focusedChild = mViewPager.getFocusedChild();
        if (focusedChild != null) {
            //轉成PDFViewer
            PDSPageViewer fASPageViewer = (PDSPageViewer) ((ViewGroup) focusedChild).getChildAt(0);
            if (fASPageViewer != null) {
                //簽名框
                RectF visibleRect = fASPageViewer.getVisibleRect();
                float width = (visibleRect.left + (visibleRect.width() / 2.0f)) - (f / 2.0f);
                float height = (visibleRect.top + (visibleRect.height() / 2.0f)) - (f2 / 2.0f);
                fASPageViewer.createElement(fASElementType, file, width, height, f, f2);
            }
            invokeMenuButton(true);
        }
    }

    //刷新頁數
    @SuppressLint("SetTextI18n")
    public void updatePageNumber(int i) {
        TextView textView = (TextView) findViewById(R.id.pageNumberTxt);
        textView.setText(i + "/" + mDocument.getNumPages());
    }

    //刷新更新過的PDF
    public void refresh() {
        setResult(RESULT_OK, new Intent());
        finish();
    }

    public void savePDFDocument() {
        PDSSaveAsPDFAsyncTask task = new PDSSaveAsPDFAsyncTask(DigitalSignatureActivity.this);
        task.execute();
    }
}
