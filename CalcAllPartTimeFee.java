import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

class CalcAllPartTimeFee {
	
	public static void main(String[] args) {

		//------------定数の定義------------
		final String COMMA = "," ; //コンマ
		
		//----------------------------------------------------------------------------
		//「WorkingResult.csv」から1月分の勤務実績を読み込み、勤務実績リストを作成
		//----------------------------------------------------------------------------
		
		List<String> workingResults = new ArrayList<String>();   //勤務実績リスト
		
		try {
			File file = new File("/Users/tats/Desktop/workspace/UdemyJava/WorkingResult.csv");       //「C:\WorkSpace」直下に「WorkingResult.csv」を格納
			BufferedReader br = new BufferedReader(new FileReader(file));

			//「WorkingResult.csv」から1行ずつ読み込み、勤務実績リストを作成
			String recode = br.readLine();
			while (recode != null) {
				workingResults.add(recode);
				recode = br.readLine();
			}
			br.close();
		} catch (IOException e) {
			System.out.println(e);
		}
        
        System.out.println("workingResults.size()：" + workingResults.size());
        System.out.println("workingResults.get(0)：" + workingResults.get(0));
        System.out.println("workingResults.get(1)：" + workingResults.get(1));
		//----------------------------------------------------------------------------
		//勤務実績リストから今月の給与を算出、結果を画面に表示
		//----------------------------------------------------------------------------
		
		int partTimeFeeByMonth = 0 ; //今月の給与
		
		//勤務実績リストから1日分ずつ給与を計算し、今月の給与を集計
		for (int i = 0; i < workingResults.size(); i++) {
			
			//勤務実績リストから1日分のデータを取り出す
			String   workingRecordSt = workingResults.get(i);
			
			//カンマ区切りでデータを分割し、配列として再定義
			//（workingRecordAr[0]：出勤日、workingRecordAr[1]：出勤時間、workingRecordAr[2]：退勤時間）
			String[] workingRecordAr = workingRecordSt.split(COMMA);
			
			//出勤時間と退勤時間を引数としてcalcPartTimeFeeByTheDayを起動。
			//戻り値として得られた値（1日あたりの給与）を今月の給与に合算。
			partTimeFeeByMonth += calcPartTimeFeeByTheDay( workingRecordAr[1] , workingRecordAr[2] ) ;
			
		}
		
		//結果を画面に表示
		System.out.println("今月の給与は、" + partTimeFeeByMonth + "円です。");
		
	}
	
	
	/*【calcPartTimeFeeByTheDayメソッド】
	**  出勤時間と退勤時間から1日分の給与を算出して返す
	*/
	public static int calcPartTimeFeeByTheDay( String st , String ed ) {
		
		//------------定数の定義------------
		final int    HOURLY_SALARY            = 900                          ; //時給
		final int    MINUTES_SALARY           = HOURLY_SALARY / 60           ; //分給（小数点以下切り捨て）
		final int    CONV_HOUR_TO_MILLISEC    = 1000 * 60 * 60               ; //時間のミリ秒換算
		final int    CONV_MINUTE_TO_MILLISEC  = 1000 * 60                    ; //分のミリ秒換算
		final int    CONV_HOUR_TO_MINUTE      = 60                           ; //時間の分換算
		final long   WORK_TIME_TYPE1_END      = 6  * CONV_HOUR_TO_MILLISEC   ; //労働時間タイプ①（ミリ秒）（労働時間～6時間以下） - 終了時間
		final long   WORK_TIME_TYPE2_START    = 6  * CONV_HOUR_TO_MILLISEC   ; //労働時間タイプ②（ミリ秒）（労働時間6時間超～8時間以下） - 開始時間
		final long   WORK_TIME_TYPE2_END      = 8  * CONV_HOUR_TO_MILLISEC   ; //労働時間タイプ②（ミリ秒）（労働時間6時間超～8時間以下） - 終了時間
		final long   WORK_TIME_TYPE3_START    = 8  * CONV_HOUR_TO_MILLISEC   ; //労働時間タイプ③（ミリ秒）（労働時間8時間超～） - 開始時間
		final long   REST_TIME_TYPE1          = 45 * CONV_MINUTE_TO_MILLISEC ; //休憩時間（ミリ秒）※労働時間タイプ②に適用
		final long   REST_TIME_TYPE2          = 60 * CONV_MINUTE_TO_MILLISEC ; //休憩時間（ミリ秒）※労働時間タイプ③に適用
		final double OVERTIME_SALARY_RATE     = 1.25                         ; //残業代倍率 ※実質労働時間が8時間を超えた場合に適用
		final int    ACTUAL_WORK_TIME_OVERTIME_OCCUR_MIN = 8  * CONV_HOUR_TO_MINUTE ; //残業が発生する実労働時間（分）（実労働時間8時間）
		
		//------------変数の定義------------
		Time startTime            = Time.valueOf( st )                         ; //出勤時間
		Time finishTime           = Time.valueOf( ed )                         ; //退勤時間
		long workTime             = finishTime.getTime() - startTime.getTime() ; //労働時間（ミリ秒）
		int  actualWorkTimeMin    = 0                                          ; //実労働時間（分）※休憩時間を差し引いた労働時間
		int  partTimeFee          = 0                                          ; //給与
		
		
		//----------------------------------------------------------------------------
		//実労働時間の計算
		//----------------------------------------------------------------------------
		
		if (workTime <= WORK_TIME_TYPE1_END) {
			//労働時間タイプ①（労働時間～6時間以下）の場合
			
			//実労働時間（分）の計算
			actualWorkTimeMin = (int)( workTime / CONV_MINUTE_TO_MILLISEC ) ;
			
		} else if (workTime > WORK_TIME_TYPE2_START && workTime <= WORK_TIME_TYPE2_END) {
			//労働時間タイプ②（労働時間6時間超～8時間以下）の場合
			
			//実労働時間（分）の計算
			actualWorkTimeMin = (int)( (  workTime - REST_TIME_TYPE1 ) / CONV_MINUTE_TO_MILLISEC ) ;
			
		} else if (workTime > WORK_TIME_TYPE3_START){
			//労働時間タイプ③（労働時間8時間超～）
			
			//実労働時間（分）の計算
			actualWorkTimeMin = (int)( ( workTime - REST_TIME_TYPE2 ) / CONV_MINUTE_TO_MILLISEC ) ;
			
		}
		
		
		//----------------------------------------------------------------------------
		//1日あたりの給与の計算 ※1円以下は切り捨て
		//----------------------------------------------------------------------------
		
		if( actualWorkTimeMin > ACTUAL_WORK_TIME_OVERTIME_OCCUR_MIN ){
			//残業代が発生する（実労働時間（分）が8時間を超える）場合
			
			partTimeFee = ( MINUTES_SALARY * ACTUAL_WORK_TIME_OVERTIME_OCCUR_MIN )
			              + (int)( MINUTES_SALARY * OVERTIME_SALARY_RATE * ( actualWorkTimeMin - ACTUAL_WORK_TIME_OVERTIME_OCCUR_MIN ) ) ;
			
		}else{
			//残業代が発生しない場合
			
			partTimeFee = MINUTES_SALARY * actualWorkTimeMin ;
		}
		
		
		//----------------------------------------------------------------------------
		//戻り値として1日あたりの給与を返す
		//----------------------------------------------------------------------------
		
		return partTimeFee ;
	}
}
