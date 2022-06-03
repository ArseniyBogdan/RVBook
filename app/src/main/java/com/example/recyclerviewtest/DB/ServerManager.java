//package com.example.recyclerviewtest.DB;
//
//import android.os.AsyncTask;
//import android.util.Log;
//
//import java.io.IOException;
//import java.io.OutputStreamWriter;
//import java.io.PrintWriter;
//import java.net.Socket;
//import java.util.Arrays;
//
//public class ServerManager extends AsyncTask<String, Void, Void>{
//
//    @Override
//    protected Void doInBackground(String... strings) {
//        try{
//            try{
//
//                Log.d("ServerManager: ", "I'm in");
//
//                Socket socket = new Socket("192.168.100.5", 1234);
//                PrintWriter outToServer = new PrintWriter(
//                        new OutputStreamWriter(
//                                socket.getOutputStream()));
//                outToServer.print(Arrays.toString(strings));
//                outToServer.flush();
//                outToServer.close();
//                socket.close();
//            }catch (IOException e){
//                e.printStackTrace();
//            }
//        }catch (Exception e){
//            return null;
//        }
//        return null;
//    }
//
//
//}