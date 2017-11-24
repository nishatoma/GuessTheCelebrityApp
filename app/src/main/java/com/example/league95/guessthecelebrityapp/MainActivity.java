package com.example.league95.guessthecelebrityapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    //We create array lists to store images and names!!
    List<String> celebUrls = new ArrayList<>();
    List<String> celebNames = new ArrayList<>();
    // this will be our random celebrity number!
    int chosenCeleb = 0;
    //Our image view
    ImageView imageView;
    //Add our buttons
    Button button0, button1, button2, button3;
    //location of correct celeb
    int locationOfCorrectAnswer = 0;
    // Array of answers
    String[] answers = new String[4];

    /*-----------------------------------------------------------*/
    //To download images we need a second async class!!
    public class ImageDownloader extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... urls) {
            //our result image!
            Bitmap image;
            //our usual url
            URL url;
            //connection
            HttpURLConnection httpURLConnection;

            try {
                url = new URL(urls[0]);
                //connect
                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.connect();
                //Input reader
                InputStream inputStream = httpURLConnection.getInputStream();
                //we get the result image from the input stream
                //From the url we connect to!
                image = BitmapFactory.decodeStream(inputStream);

                return image;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    /*-----------------------------------------------------------*/
    //We start by downloading the content!
    public class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {

            StringBuilder result = new StringBuilder();
            //Then we have our url
            URL url;
            //Then connection
            HttpURLConnection httpURLConnection;

            try {
                url = new URL(urls[0]);
                //Connect to the url
                httpURLConnection = (HttpURLConnection) url.openConnection();
                //Take in input from that url data
                InputStream inputStream = httpURLConnection.getInputStream();
                //then read the data
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

                //ddata
                int data = inputStreamReader.read();
                char c;
                //while there is still data
                while (data != -1) {
                    c = (char) data;
                    result.append(c);
                    //update data on each iteration
                    data = inputStreamReader.read();
                }
                return result.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Image view
        imageView = findViewById(R.id.imageView);
        //Create our buttons
        button0 = (Button) findViewById(R.id.button);
        button1 = (Button) findViewById(R.id.button1);
        button2 = (Button) findViewById(R.id.button2);
        button3 = (Button) findViewById(R.id.button3);
        //Download images here first, no need to do it each time
        //we select a celebrity
        //Now we want to run our task downloader thingy
        DownloadTask downloadTask = new DownloadTask();

        String url = "http://www.posh24.se/kandisar";
        String result;
        //download image on start
        // get the data from url!!
        try {
            result = downloadTask.execute(url).get();
            //Split at a point we dont want
            String[] splitResult = result.split("<div class=\"sidebarContainer\">");
            //Now we look for urls we want via pattern matching
            Pattern p = Pattern.compile("<img src=\"(.*?)\"");
            Matcher m = p.matcher(splitResult[0]);

            while (m.find()) {
                //Add celebrities photo urls here
                celebUrls.add(m.group(1));
            }

            //then we match the alt names
            p = Pattern.compile("alt=\"(.*?)\"");
            m = p.matcher(splitResult[0]);

            while (m.find()) {
                //Add their names here
                celebNames.add(m.group(1));
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }


        downloadImg();

    }

    public void guess(View view) {
        if (view.getTag().toString().equals(String.valueOf(locationOfCorrectAnswer))) {
            Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Wrong! It was " + celebNames.get(chosenCeleb), Toast.LENGTH_SHORT).show();
        }
        downloadImg();
    }

    public void downloadImg() {


        //After we got photo urls and names
        Random random = new Random();
        // choose a random celeb this way
        chosenCeleb = random.nextInt(celebUrls.size());
            /*--------------------------------------------*/
        //Create image downloader
        ImageDownloader imageDownloader = new ImageDownloader();

        Bitmap celebImage;
        try {
            celebImage = imageDownloader.execute(celebUrls.get(chosenCeleb)).get();
            //update image view
            imageView.setImageBitmap(celebImage);
            //Location is one of the 4 choices, made randomly!
            locationOfCorrectAnswer = random.nextInt(4);
            // the incorrect random asnwers
            int incorrectAnswerLocation;
            for (int i = 0; i < answers.length; i++) {
                if (i == locationOfCorrectAnswer) {
                    answers[i] = celebNames.get(chosenCeleb);
                } else {
                    // we create a random asnwer!
                    incorrectAnswerLocation = random.nextInt(celebUrls.size());
                    while (incorrectAnswerLocation == locationOfCorrectAnswer) {
                        //Just in case they are the same, get another incorrect answr
                        incorrectAnswerLocation = random.nextInt(celebUrls.size());
                    }
                    answers[i] = celebNames.get(incorrectAnswerLocation);
                }
            }
            button0.setText(answers[0]);
            button1.setText(answers[1]);
            button2.setText(answers[2]);
            button3.setText(answers[3]);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

}
