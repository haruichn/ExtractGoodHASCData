# 座標系変換に使えるデータ抽出ツール
座標系変換(軸補正)に使える, 加速度・角速度が揃っているかつ, サンプリング周波数もほぼ等しいデータのみを抽出するツール.

## 使い方
#### IntelliJ IDEAから使う場合
1. このプロジェクトを読み込む
2. dataディレクトリにHASCコーパスのデータを入れる.
3. 実行
4. outputディレクトリが作成され, そこに補正されたデータが出力される.

#### コマンドラインから使う場合
1. ExtractGoodFile.javaを適当なティレクトリにコピー
2. ExtractGoodFile.javaがある階層にcdで移動
3. ExtractGoodFile.javaと同ディレクトリにdataディレクトリを用意.
4. dataディレクトリにHASCコーパスのデータを入れる.
5. 以下のコマンドを実行
6. outputディレクトリが作成され, そこに補正されたデータが出力される.

##### コマンド
```
javac ExtractGoodFile.java
java ExtractGoodFile
```


### データのディレクトリ構成
```
.  
data  
├── 1_stay  
│  ├── person0001  
│  │   ├── HASCXXXXXX-acc.csv  
│  │   ├── HASCXXXXXX-gyro.csv  
│  │   ├── HASCXXXXXX-mag.csv  
│  │   └── ...  
│  ├── person0002  
│  └── ...  
├── 2_walk  
│　├── person0001  
│　│   ├── HASCXXXXXX-acc.csv  
│　│   ├── HASCXXXXXX-gyro.csv  
│　│   ├── HASCXXXXXX-mag.csv  
│　│   └── ...  
│　├── person0002  
│　└── ...  
└── ...
```


### 　
Developed by icchi  
2016/04/01
