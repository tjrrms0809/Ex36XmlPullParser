package com.ahnsafety.ex36xmlpullparser;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    //영화진흥위원회 통합전산망에서 발급받은 인증키
    String apiKey="430156241533f1d058c603178cc3ca0e";

    ListView listView;
    ArrayAdapter adapter;

    ArrayList<String> items= new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView= findViewById(R.id.listview);
        adapter= new ArrayAdapter(this, android.R.layout.simple_list_item_1, items);
        listView.setAdapter(adapter);
    }

    public void clickBtn(View view) {
        //네트워크를 통해서 xml문서를 읽어오기..
        new Thread(){
            @Override
            public void run() {
                //영화진흥위원회 open API를 통해
                //일일박스오피스 정보를 가진 xml문서를
                //읽어와서 분석하여 Listview에 보여주기
                items.clear();

                Date date= new Date();//현재 날짜와 시간을 가진 객체
                date.setTime(date.getTime()-(1000*60*60*24));//1일전
                SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMdd");

                String dateStr=sdf.format(date);//"20190919

                String address="http://www.kobis.or.kr/kobisopenapi/webservice/rest/boxoffice/searchDailyBoxOfficeList.xml"
                        +"?key="+apiKey
                        +"&targetDt="+dateStr
                        +"&itemPerPage=10";
                //SAMPLE url
                //address="http://www.kobis.or.kr/kobisopenapi/webservice/rest/boxoffice/searchDailyBoxOfficeList.xml?key=430156241533f1d058c603178cc3ca0e&targetDt=20120101";
                //위 xml문서의 주소(address)에 스트림을 연결하여 데이터를 읽어오기


                try {
                    //해임달객체 생성
                    URL url= new URL(address);
                    //무지개로드 열기
                    InputStream is= url.openStream(); //바이트스트림
                    //문자스트림으로 변환
                    InputStreamReader isr= new InputStreamReader(is);
                    //읽어들인 XML문서를 분석(parse)해주는 객체 생성
                    XmlPullParserFactory factory= XmlPullParserFactory.newInstance();
                    XmlPullParser xpp= factory.newPullParser();
                    xpp.setInput(isr);
                    //xpp를 이용해서 xml문서를 분석
                    int eventType= xpp.getEventType();

                    String tagName;
                    StringBuffer buffer= null;

                    while(eventType!=XmlPullParser.END_DOCUMENT){

                        switch (eventType){
                            case XmlPullParser.START_DOCUMENT:

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(MainActivity.this, "파싱시작!", Toast.LENGTH_SHORT).show();
                                    }
                                });

                                break;

                            case XmlPullParser.START_TAG:
                                tagName= xpp.getName();

                                if(tagName.equals("dailyBoxOffice")){
                                    buffer= new StringBuffer();
                                }else if(tagName.equals("rank")){
                                    buffer.append("순위:");
                                    xpp.next();
                                    buffer.append(xpp.getText()+"\n");

                                }else if(tagName.equals("movieNm")){
                                    buffer.append("제목:");
                                    xpp.next();
                                    buffer.append(xpp.getText()+"\n");

                                }else if(tagName.equals("openDt")){
                                    buffer.append("개봉일:");
                                    xpp.next();
                                    buffer.append(xpp.getText()+"\n");

                                }else if(tagName.equals("audiAcc")){
                                    buffer.append("누적관객수:");
                                    xpp.next();
                                    buffer.append(xpp.getText()+"\n");
                                }

                                break;

                            case XmlPullParser.TEXT:
                                break;

                            case XmlPullParser.END_TAG:
                                tagName= xpp.getName();
                                if( tagName.equals("dailyBoxOffice")){

                                    items.add(buffer.toString());

                                    //리스트뷰 갱신
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            adapter.notifyDataSetChanged();
                                        }
                                    });
                                }
                                break;
                        }

                        eventType= xpp.next();
                    }//while..

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "파싱종료!!", Toast.LENGTH_SHORT).show();
                        }
                    });


                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                }


            }//run method...
        }.start();
    }
}
