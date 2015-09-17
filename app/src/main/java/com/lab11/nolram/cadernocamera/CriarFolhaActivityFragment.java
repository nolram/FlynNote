package com.lab11.nolram.cadernocamera;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.lab11.nolram.components.BitmapHelper;
import com.lab11.nolram.components.DeviceDimensionsHelper;
import com.lab11.nolram.database.Database;
import com.lab11.nolram.database.controller.FolhaDataSource;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * A placeholder fragment containing a simple view.
 */
public class CriarFolhaActivityFragment extends Fragment {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int RESULT_LOAD_IMAGE = 2;

    private long fk_caderno;

    private String nomeCaderno;

    private EditText edtTitulo;
    private EditText edtTags;
    private ImageView imgThumb;
    private Button btnAddFolha;
    private Button btnGetCamera;
    private Button btnGetGallery;
    private Toolbar toolbar;

    private FolhaDataSource folhaDataSource;

    String mCurrentPhotoPath = "";

    public CriarFolhaActivityFragment() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
    }

    @Override
    public void onResume() {
        folhaDataSource.open();
        super.onResume();
    }

    @Override
    public void onPause() {
        folhaDataSource.close();
        super.onPause();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case REQUEST_IMAGE_CAPTURE:
                if(resultCode == getActivity().RESULT_OK){
                    File imgFile = new  File(mCurrentPhotoPath);
                    int screenWidth = DeviceDimensionsHelper.getDisplayWidth(getActivity());
                    if(imgFile.exists()){
                        Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                        Bitmap ThumbImage = ThumbnailUtils.extractThumbnail(
                                myBitmap, screenWidth, 500);
                        imgThumb.setImageBitmap(ThumbImage);
                    }
                    //Bitmap photo = (Bitmap) data.getExtras().get("data");
                    //saveBitmap(photo);
                }else if (resultCode == getActivity().RESULT_CANCELED){
                    File imgFile = new File(mCurrentPhotoPath);
                    //Log.d("local", mCurrentPhotoPath);
                    if(imgFile.exists()){
                        imgFile.delete();
                        mCurrentPhotoPath = "";
                        imgThumb.setImageDrawable(null);
                    }
                }
                break;
            case RESULT_LOAD_IMAGE:
                if(resultCode == getActivity().RESULT_OK && null != data){
                    Uri selectedImage = data.getData();
                    try {
                        int screenWidth = DeviceDimensionsHelper.getDisplayWidth(getActivity());
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImage);
                        Bitmap ThumbImage = ThumbnailUtils.extractThumbnail(
                                bitmap, screenWidth, 500);
                        imgThumb.setImageBitmap(ThumbImage);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mCurrentPhotoPath = getStringFromUri(selectedImage);
                    //File imgFile = new  File(mCurrentPhotoPath);
                    //Log.d("local", mCurrentPhotoPath);
                    //if(imgFile.exists()){
                    //    Bitmap myBitmap = BitmapHelper.decodeSampledBitmapFromLocal(imgFile.getAbsolutePath(), 100, 200);
                    //}
                }
                break;
            default:
                break;
        }
    }


    private Bitmap getThumbnailBitmap(String path, int thumbnailSize) {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, bounds);
        if ((bounds.outWidth == -1) || (bounds.outHeight == -1)) {
            return null;
        }
        int originalSize = (bounds.outHeight > bounds.outWidth) ? bounds.outHeight
                : bounds.outWidth;
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inSampleSize = originalSize / thumbnailSize;
        return BitmapFactory.decodeFile(path, opts);
    }

    private String getStringFromUri(Uri contentUri) {
        String path = null;
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = getActivity().getContentResolver().query(contentUri, projection, null, null, null);
        if (cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            path = cursor.getString(columnIndex);
        }
        cursor.close();
        return path;
    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.e("criar", "Não foi possível criar o arquivo");
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }


    public void saveBitmap(Bitmap photo) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = nomeCaderno.toUpperCase()+"_" + timeStamp +".png";
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                getString(R.string.app_name));

        if(!storageDir.isDirectory()){
            storageDir.mkdirs();
        }

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.PNG, 100, bytes);

        // you can create a new file name "test.jpg" in sdcard folder.
        File f = new File(storageDir, imageFileName);
        try {
            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());

            // remember close de FileOutput
            fo.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // write the bytes in file

    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = nomeCaderno.toUpperCase()+"_" + timeStamp + "_";
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                getString(R.string.app_name));

        if(!storageDir.isDirectory()){
            storageDir.mkdirs();
        }

        if(!mCurrentPhotoPath.isEmpty()){
            File mExistente = new File(mCurrentPhotoPath);
            if(mExistente.exists()){
                boolean temp = mExistente.delete();
                if(temp){
                    Log.d("img_deletado", "Imagem deletada");
                }
            }
        }

        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_criar_folha, container, false);
        edtTitulo = (EditText) view.findViewById(R.id.edtxt_titulo_folha);
        edtTags = (EditText) view.findViewById(R.id.edtxt_tags);
        imgThumb = (ImageView) view.findViewById(R.id.img_thumb);
        btnAddFolha = (Button) view.findViewById(R.id.btn_adicionar_folha);
        btnGetCamera = (Button) view.findViewById(R.id.btn_camera);
        btnGetGallery = (Button) view.findViewById(R.id.btn_galeria);

        toolbar = (Toolbar) view.findViewById(R.id.toolbar);

        folhaDataSource = new FolhaDataSource(view.getContext());
        folhaDataSource.open();

        Bundle bundle = getActivity().getIntent().getExtras();
        fk_caderno = bundle.getLong(Database.FOLHA_FK_CADERNO);
        nomeCaderno = bundle.getString(Database.CADERNO_TITULO);


        btnGetCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mCurrentPhotoPath.isEmpty()){
                    dispatchTakePictureIntent();
                }else{
                    //Log.d("local", mCurrentPhotoPath);
                    new AlertDialog.Builder(getActivity())
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle(R.string.alert_title_img)
                            .setMessage(R.string.alert_mensage_img)
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dispatchTakePictureIntent();
                                }

                            })
                            .setNegativeButton(R.string.no, null)
                            .show();
                }
            }
        });

        btnGetGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mCurrentPhotoPath.isEmpty()){
                    Intent i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(i, RESULT_LOAD_IMAGE);
                }else{
                    new AlertDialog.Builder(getActivity())
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle(R.string.alert_title_img)
                            .setMessage(R.string.alert_mensage_img)
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                    startActivityForResult(i, RESULT_LOAD_IMAGE);
                                }

                            })
                            .setNegativeButton(R.string.no, null)
                            .show();
                }
            }
        });

        btnAddFolha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String titulo = edtTitulo.getText().toString();
                String tags = edtTags.getText().toString();
                String[] res_tags = null;
                if(!tags.isEmpty()){
                    res_tags = tags.split("[#.;,]");
                }
                //Toast.makeText(v.getContext(), Arrays.toString(res_tags), Toast.LENGTH_SHORT).show();
                if(!mCurrentPhotoPath.isEmpty()) {
                    folhaDataSource.criarFolha(mCurrentPhotoPath, fk_caderno, titulo, res_tags);
                    getActivity().finish();
                }else {
                    Toast.makeText(v.getContext(), getResources().getString(R.string.alert_empty_img),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        return view;
    }
}
