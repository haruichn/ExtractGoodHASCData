/*
* Title：ExtractGoodData
* 説明 ： 座標系変換(軸補正)に使える, 加速度・角速度が揃っているかつ, サンプリング周波数もほぼ等しいデータのみを抽出するツール.
* @date Created on: 2016/04/01
* @author Author: Haruyuki Ichino
* @version 0.0
*
*/

import java.io.*;
import java.nio.file.*;
import java.util.StringTokenizer;


public class ExtractFile_for_AxisCorrection {

    // 十分とみなすファイルの行数の境界
    static int enough_th = 50;

    public static void main(String[] args){

        // データの場所指定
        String data_path = "./data/";
        // 軸補正後のデータの格納場所
        String output_path = "./output/";

        // もし出力フォルダがなければ作成
        File output_dir = new File(output_path);
        if(output_dir.exists() == false){
            output_dir.mkdir();
        }


        // 通常のファイル(隠しファイルでない)のみを取り出すフィルタの作成
        FilenameFilter normalFileFilter = new FilenameFilter() {
            public boolean accept(File file, String name) {
                if (file.isHidden() == false){
                    return true;
                } else {
                    return false;
                }
            }
        };
        // 加速度ファイルのみを取り出すフィルタの作成
        FilenameFilter accFileFilter = new FilenameFilter() {
            public boolean accept(File file, String name) {
                if (name.matches(".*acc.*")){
                    return true;
                } else {
                    return false;
                }
            }
        };

        System.out.println("========================================================================");
        System.out.println("1.ファイルの読み込み");
        System.out.println("========================================================================");
        File data_dir = new File(data_path);

        float acc_file_count = 0;
        float good_acc_file_count = 0;

        // data内のファイルを取得
        File[] activity_dirs = data_dir.listFiles(normalFileFilter);

        try {
            System.out.println("Activity count = " + activity_dirs.length);
        } catch (NullPointerException e){
            System.out.println("dataディレクトリがないよ");
        }

        // 各行動ディレクトリにアクセス
        for (File activity_dir : activity_dirs){
            if(activity_dir.isHidden() == false){
                System.out.println("===================================================");
                System.out.println(activity_dir);
                System.out.println("===================================================");

                // 行動ディレクトリ内のファイルを取得
                File[] person_dirs = activity_dir.listFiles(normalFileFilter);

                System.out.println("person count = " + person_dirs.length);

                // 各personディレクトリにアクセス
                for(File person_dir : person_dirs){
                    if(person_dir.isHidden() == false){
                        System.out.println("======================================");
                        System.out.println(person_dir.getName());
                        System.out.println("======================================");

                        // personディレクトリ内のファイルを取得
                        File[] files = person_dir.listFiles(normalFileFilter);
                        File[] acc_files = person_dir.listFiles(accFileFilter);

                        System.out.println("acc file count = " + acc_files.length);
                        System.out.println();

                        // 各加速度ファイルにアクセス
                        for(File acc_file : acc_files){
                            acc_file_count++;

                            String acc_file_name = acc_file.getName();
                            System.out.println(acc_file_name);

                            // 名前からID部分の取り出し
                            int idx_hascID = acc_file_name.indexOf("-");
                            String file_id = acc_file_name.substring(0,idx_hascID);

                            dispFileLineSize(acc_file);



                            //同じIDの加速度,ジャイロがあるかどうか
                            if(isHaveAccGyroMag(files, acc_file_name)){ //引数のファイル群は加速度以外のファイルも含む物を渡す
                                System.out.println("加速度・角速度データ");

                                // 姿勢行列計算のテスト

                                double[][] acc_data = null;
                                double[][] gyro_data = null;

                                for(File file : files){
                                    String file_name = file.getName();

                                    if(file_name.matches(file_id + "-acc.*")){
                                        acc_data = getCsvData(file);
                                    }
                                    else if(file_name.matches(file_id + "-gyro.*")){
                                        gyro_data = getCsvData(file);
                                    }
                                }

                                // それぞれのデータの行数が十分にあるか
                                if(isEnoughData(acc_data, gyro_data)){

                                    System.out.println("◎ データ量OK!◎");

                                    // 出力ディレクトリに同じ名前のファイルを作成
                                    // 出力ディレクトリに行動ディレクトリを作成
                                    File output_activity_dir = new File(output_path+activity_dir.getName()+"/");
                                    if(output_activity_dir.exists() == false){
                                        output_activity_dir.mkdir();
                                    }
                                    // 出力ディレクトリにpersonディレクトリを作成
                                    File output_person_dir = new File(output_activity_dir.getPath()+"/"+person_dir.getName()+"/");
                                    if(output_person_dir.exists() == false){
                                        output_person_dir.mkdir();
                                    }
                                    // 出力ディレクトリにacc,gyro,mag,meta.labelファイルをコピー
                                    // 入力ファイルのパス設定
                                    Path accFilePath = FileSystems.getDefault().getPath(person_dir.getPath()+"/"+file_id+"-acc.csv");
                                    Path gyroFilePath = FileSystems.getDefault().getPath(person_dir.getPath()+"/"+file_id+"-gyro.csv");
                                    Path metaFilePath = FileSystems.getDefault().getPath(person_dir.getPath()+"/"+file_id+".meta");
                                    Path labelFilePath = FileSystems.getDefault().getPath(person_dir.getPath()+"/"+file_id+".label");

                                    // 出力ファイルのパス設定
                                    Path output_accFilePath = FileSystems.getDefault().getPath(output_person_dir.getPath()+"/"+file_id+"-acc.csv");
                                    Path output_gyroFilePath = FileSystems.getDefault().getPath(output_person_dir.getPath()+"/"+file_id+"-gyro.csv");
                                    Path output_metaFilePath = FileSystems.getDefault().getPath(output_person_dir.getPath()+"/"+file_id+".meta");
                                    Path output_labelFilePath = FileSystems.getDefault().getPath(output_person_dir.getPath()+"/"+file_id+".label");

                                    // コピー処理
                                    // 加速度
                                    try {
                                        Files.copy(accFilePath, output_accFilePath);
                                        System.out.println("コピー完了: "+output_accFilePath.toString());
                                    } catch (FileAlreadyExistsException e){
                                        System.out.println("\tError: ファイルがすでに存在しています!");
                                    } catch (NoSuchFileException e) {
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    // 角速度
                                    try {
                                        Files.copy(gyroFilePath, output_gyroFilePath);
                                        System.out.println("コピー完了: "+output_gyroFilePath.toString());
                                    } catch (FileAlreadyExistsException e){
                                        System.out.println("\tError: ファイルがすでに存在しています!");
                                    } catch (NoSuchFileException e) {
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    // meta
                                    try {
                                        Files.copy(metaFilePath, output_metaFilePath);
                                        System.out.println("コピー完了: "+output_metaFilePath.toString());
                                    } catch (FileAlreadyExistsException e){
                                        System.out.println("\tError: ファイルがすでに存在しています!");
                                    } catch (NoSuchFileException e) {
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    // label
                                    try {
                                        Files.copy(labelFilePath, output_labelFilePath);
                                        System.out.println("コピー完了: "+output_labelFilePath.toString());
                                    } catch (FileAlreadyExistsException e){
                                        System.out.println("\tError: ファイルがすでに存在しています!");
                                    } catch (NoSuchFileException e) {
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                    good_acc_file_count++;
                                    System.out.println();
                                }
                                else{
                                    System.out.println("× データ量少なすぎぃ!×");
                                }
                            }
                            else{
                                System.out.println("加速度・角速度データのどれかなし!!");
                            }
                            System.out.println();
                        }
                    }
                }
            }
        }
        // 使えるデータの割合の表示
        String good_data_rate = String.format("%.2f", good_acc_file_count/acc_file_count*100);
        System.out.println("姿勢計算に使えるデータセットの割合: " + good_data_rate + "% ("+String.valueOf((int)good_acc_file_count)+"/"+String.valueOf((int)acc_file_count)+")");
    }


    static void dispFileLineSize(File file){
        System.out.print("(行数, 列数) = (");
        System.out.print(getLineNumber(file));
        System.out.print(", ");
        System.out.print(getRowNumber(file));
        System.out.println(")");
    }

    static int getLineNumber(File file){
        int line_number = -1;
        String aLine;
        LineNumberReader file_lnr = null;

        try{
            file_lnr = new LineNumberReader(new FileReader(file));
            // 最後の行まで選択行をすすめる
            while(null!=(aLine = file_lnr.readLine())){}
            line_number = file_lnr.getLineNumber();
        }catch(IOException e){
            //例外発生時処理
            e.printStackTrace();
        }
        return line_number;
    }

    static int getRowNumber(File file){
        int row_number = -1;

        try {
            //ファイルを読み込む
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);

            //最初の１行目の文字列を取得
            String line = br.readLine();

            //区切り文字","で分割する
            StringTokenizer token = new StringTokenizer(line, ",");

            // ,で分割された数(1行の要素数)を取得
            row_number = token.countTokens();

            //終了処理
            br.close();

        } catch (IOException ex) {
            //例外発生時処理
            ex.printStackTrace();
        }
        return row_number;
    }

    static double[][] getCsvData(File file){

        int row_number = getRowNumber(file);
        int line_number = getLineNumber(file);

        double data[][] = new double[line_number][row_number];
        try {
            //ファイルを読み込む
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);

            //読み込んだファイルを１行ずつ処理する
            String line_str;
            StringTokenizer token;
            for(int i=0; (line_str = br.readLine()) != null; i++){
                //区切り文字","で分割する
                token = new StringTokenizer(line_str, ",");

                // System.out.println(file.getName());
                //分割した文字を配列に代入
                for(int j=0; token.hasMoreTokens(); j++){
                    String element = token.nextToken();
                    if(j==0){
                        data[i][j] = Double.valueOf(element);
                        // System.out.print(element+" (");
                        // System.out.print(element.substring(3, element.length()-3)+" ");
                        // System.out.print(Float.parseFloat(element.substring(5, element.length()-3)));
                        // System.out.print(") ");
                    }
                    else{
                        data[i][j] = Double.valueOf(element);
                        // System.out.print(Float.valueOf(element)+" ");
                    }
                }
                // System.out.println();
            }

            //終了処理
            br.close();

        } catch (IOException ex) {
            //例外発生時処理
            ex.printStackTrace();
        }
        return data;
    }

    static boolean isHaveAccGyroMag(File[] files, String file_name){
        /**
         * あるファイルIDが加速度,ジャイロ,地磁気を持っているかどうかの判定
         * @param Files ディレクトリ内のファイルリスト
         * @param file_id 検索するファイルID
         * @return 結果
         */
        boolean isAcc = false;
        boolean isGyro = false;

        // 名前からID部分の取り出し
        int idx_hascID = file_name.indexOf("-");
        String file_id = file_name.substring(0,idx_hascID);

        for(File file : files){
            String target_file_name = file.getName();
            if(target_file_name.matches(file_id + "-acc.*")){
                isAcc = true;
            }
            else if(target_file_name.matches(file_id + "-gyro.*")){
                isGyro = true;
            }
        }

        if((isAcc == true) && (isGyro == true)) return true;
        else return false;
    }

    static boolean isEnoughData(double[][] acc_data, double[][] gyro_data){
        boolean haveEnoughData = false;

        int acc_line_num = acc_data.length;
        int gyro_line_num = gyro_data.length;

        if(acc_line_num-gyro_line_num < enough_th){
            haveEnoughData = true;
        }

        System.out.print("Data lines = (");
        System.out.print("acc:");
        System.out.print(acc_line_num);
        System.out.print(", gyro:");
        System.out.print(gyro_line_num);
        System.out.println(")");

        return haveEnoughData;
    }





}
