package br.com.lbernardo.Utils;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import br.com.fortics.smartzap.appclient.Manifest;

/**
 * Created by lucas on 22/12/17.
 */

public class Utils {

    /**
     * Retorna hora com base no datetime
     * @param datetime
     * @return
     */
    public static String getTime(String datetime){
        String[] exp = datetime.split(" ");
        // Hora com segundos
        String hourSeconds = exp[1];
        // Hora sem segundos
        String hour = hourSeconds.substring(0,5);
        return hour;
    }


    /**
     * Solicita permissão
     * @param activity
     * @param permission
     */
    public static void requestPermission(Activity activity,String permission) {

        // Verifica se usuário já não tem permissão total sobre o aplicativo
        if (ContextCompat.checkSelfPermission(activity,
                permission)
                != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    permission)) {
                // Permissão
                ActivityCompat.requestPermissions(activity,
                        new String[]{permission},
                        1);
            }

        }
    }


    /**
     * Upload de Imagem
     * @param sourceFileUri
     * @param name
     * @param urlServer
     * @return
     */
    public static int uploadFile(String urlServer,String name,String sourceFileUri,String[] postName,String[] postValue){
        String fileName = sourceFileUri;

        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String boundary = "===" + System.currentTimeMillis() + "===";
        int bytesRead;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File sourceFile = new File(sourceFileUri);

        if (!sourceFile.isFile()) {
            Log.e("Erro no arquivo","Arquivo não é arquivo");
            Log.e("Arquivo",fileName);
            return 0;

        }
        else
        {
            int serverResponseCode = 0;
            try {

                // open a URL connection to the Servlet
                FileInputStream fileInputStream = new FileInputStream(sourceFile);
                URL url = new URL(urlServer);

                // Open a HTTP  connection to  the URL
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("uploaded_file", fileName);
                OutputStream outputStream = conn.getOutputStream();
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"),true);


                for(int i  = 0 ; i< postName.length;i++) {
                    String postN = postName[i];
                    String postV = postValue[i];

                    writer.append("--" + boundary).append(lineEnd);
                    writer.append("Content-Disposition: form-data; name=\""+postN+"\"").append(lineEnd);
                    writer.append("Content-type:text/plain;charset=UTF-8").append(lineEnd).append(lineEnd);
                    writer.append(postV).append(lineEnd);
                    writer.flush();

                }

                String fileNameF = sourceFile.getName();
                writer.append("--"+boundary).append(lineEnd);
                writer.append("Content-Disposition: form-data; name=\"upload\"; filename=\""+fileName+"\"").append(lineEnd);
                writer.append("Content-Type:"+ URLConnection.guessContentTypeFromName(fileName)).append(lineEnd);
                writer.append("Content-Transfer-Encoding: binary").append(lineEnd);
                writer.append(lineEnd);
                writer.flush();

                buffer = new byte[maxBufferSize];
                bytesRead = -1;
                while((bytesRead = fileInputStream.read(buffer)) != -1){
                    outputStream.write(buffer,0,bytesRead);
                }
                outputStream.flush();
                fileInputStream.close();

                writer.append(lineEnd);
                writer.flush();

                writer.append(lineEnd).flush();
                writer.append("--"+boundary+"--").append(lineEnd);
                writer.close();

                // Responses from the server (code and message)
                serverResponseCode = conn.getResponseCode();
                String serverResponseMessage = conn.getResponseMessage();

                Log.d("uploadFile", "HTTP Response is : "
                        + serverResponseMessage + ": " + serverResponseCode);

                if(serverResponseCode == 200){

                    Log.d("Resultado","Upload completo");
                }


            } catch (MalformedURLException ex) {

                ex.printStackTrace();

                Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
            } catch (Exception e) {


                e.printStackTrace();
                Log.e("Erro no upload",e.toString());

            }
            return serverResponseCode;

        } // End else block
    }

    /**
     * Retorna caminho da Uri
     * @param contentURI
     * @param activity
     * @return
     */
    public static String getRealPathFromURI(Uri contentURI,Activity activity) {
        String result;
        Cursor cursor = activity.getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

}
