package com.example.view;

import java.util.ArrayList;
import java.util.List;

import com.example.constant.ConstantUtil;
import com.example.factory.GameObjectFactory;
import com.example.mybeatplane.R;
import com.example.object.BigPlane;
import com.example.object.BossPlane;
import com.example.object.EnemyPlane;
import com.example.object.GameObject;
import com.example.object.MiddlePlane;
import com.example.object.MyPlane;
import com.example.object.SmallPlane;
import com.example.sounds.GameSoundPool;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Message;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
/*游戏进行的主界面*/
public class MainView extends BaseView{
	private int missileCount; 		// 导弹的数量
	private int middlePlaneScore;	// 中型敌机的积分
	private int bigPlaneScore;		// 大型敌机的积分
	private int bossPlaneScore;		// boss型敌机的积分	
	private int missileScore;		// 导弹的积分
	private int sumScore;			// 游戏总得分
	private int speedTime;			// 游戏速度的倍数
	private float bg_y;				// 图片的坐标
	private float bg_y2;
	private float play_bt_w;
	private float play_bt_h;	 
	private float missile_bt_y;		 	
	private boolean isPlay;			// 标记游戏运行状态
	private boolean isTouchPlane;	// 判断玩家是否按下屏幕
	private Bitmap background; 		// 背景图片
	private Bitmap background2; 	// 背景图片
	private Bitmap playButton; 		// 开始/暂停游戏的按钮图片
	private Bitmap missile_bt;		// 导弹按钮图标
	private MyPlane myPlane;		// 玩家的飞机
	private BossPlane bossPlane;	// boss飞机
	private List<EnemyPlane> enemyPlanes;
	private GameObjectFactory factory;
	public MainView(Context context,GameSoundPool sounds) {
		super(context,sounds);
		// TODO Auto-generated constructor stub
		isPlay = true;
		speedTime = 1;
		sumScore =101;
		factory = new GameObjectFactory();						  //工厂类
		enemyPlanes = new ArrayList<EnemyPlane>();
		myPlane = (MyPlane) factory.createMyPlane(getResources());//生产玩家的飞机
		myPlane.setMainView(this);
		for(int i = 0;i < SmallPlane.sumCount;i++){
			//生产小型敌机
			SmallPlane smallPlane = (SmallPlane) factory.createSmallPlane(getResources());
			enemyPlanes.add(smallPlane);
		}
		for(int i = 0;i < MiddlePlane.sumCount;i++){
			//生产中型敌机
			MiddlePlane middlePlane = (MiddlePlane) factory.createMiddlePlane(getResources());
			enemyPlanes.add(middlePlane);
		}
		for(int i = 0;i < BigPlane.sumCount;i++){
			//生产大型敌机
			BigPlane bigPlane = (BigPlane) factory.createBigPlane(getResources());
			enemyPlanes.add(bigPlane);
		}
		//生产BOSS敌机
		bossPlane = (BossPlane)factory.createBossPlane(getResources());
		bossPlane.setMyPlane(myPlane);
		enemyPlanes.add(bossPlane);
		thread = new Thread(this);	
	}
	// 视图改变的方法
	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		super.surfaceChanged(arg0, arg1, arg2, arg3);
	}
	// 视图创建的方法
	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		// TODO Auto-generated method stub
		super.surfaceCreated(arg0);
		initBitmap(); // 初始化图片资源
		for(GameObject obj:enemyPlanes){			
			obj.setScreenWH(screen_width,screen_height);
		}
		myPlane.setScreenWH(screen_width,screen_height);
		myPlane.setAlive(true);
		if(thread.isAlive()){
			thread.start();
		}
		else{
			thread = new Thread(this);
			thread.start();
		}
	}
	// 视图销毁的方法
	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		// TODO Auto-generated method stub
		super.surfaceDestroyed(arg0);
		release();
	}
	// 响应触屏事件的方法
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(event.getAction() == MotionEvent.ACTION_UP){
			isTouchPlane = false;
			return true;
		}
		else if(event.getAction() == MotionEvent.ACTION_DOWN){
			float x = event.getX();
			float y = event.getY();
			if(x > 10 && x < 10 + play_bt_w && y > 10 && y < 10 + play_bt_h){
				if(isPlay){
					isPlay = false;
				}		
				else{
					isPlay = true;	
					synchronized(thread){
						thread.notify();
					}
				}
				return true;
			}
			//判断玩家飞机是否被按下
			else if(x > myPlane.getObject_x() && x < myPlane.getObject_x() + myPlane.getObject_width() 
					&& y > myPlane.getObject_y() && y < myPlane.getObject_y() + myPlane.getObject_height()){
				if(isPlay){
					isTouchPlane = true;
				}
				return true;
			}
			//判断导弹按钮是否被按下
			else if(x > 10 && x < 10 + missile_bt.getWidth() 
					&& y > missile_bt_y && y < missile_bt_y + missile_bt.getHeight()){
				if(missileCount > 0){
					missileCount--;
					sounds.playSound(5, 0);
					for(EnemyPlane pobj:enemyPlanes){
						if(pobj.isCanCollide()){
							pobj.attacked(100);		   // 敌机增加伤害
							if(pobj.isExplosion()){
								addGameScore(pobj.getScore());// 获得分数
							}			
						}
					}	
				}
				return true;
			}
		}
		//响应手指在屏幕移动的事件
		else if(event.getAction() == MotionEvent.ACTION_MOVE && event.getPointerCount() == 1){
			//判断触摸点是否为玩家的飞机
			if(isTouchPlane){
				float x = event.getX();
				float y = event.getY();
				if(x > myPlane.getMiddle_x() + 20){
					if(myPlane.getMiddle_x() + myPlane.getSpeed() <= screen_width){
						myPlane.setMiddle_x(myPlane.getMiddle_x() + myPlane.getSpeed());
					}					
				}
				else if(x < myPlane.getMiddle_x() - 20){
					if(myPlane.getMiddle_x() - myPlane.getSpeed() >= 0){
						myPlane.setMiddle_x(myPlane.getMiddle_x() - myPlane.getSpeed());
					}				
				}
				if(y > myPlane.getMiddle_y() + 20){
					if(myPlane.getMiddle_y() + myPlane.getSpeed() <= screen_height){
						myPlane.setMiddle_y(myPlane.getMiddle_y() + myPlane.getSpeed());
					}		
				}
				else if(y < myPlane.getMiddle_y() - 20){
					if(myPlane.getMiddle_y() - myPlane.getSpeed() >= 0){
						myPlane.setMiddle_y(myPlane.getMiddle_y() - myPlane.getSpeed());
					}
				}
				return true;
			}	
		}
		return false;
	}
	// 初始化图片资源方法
	@Override
	public void initBitmap() {
		// TODO Auto-generated method stub
		playButton = BitmapFactory.decodeResource(getResources(),R.drawable.pla);
		background = BitmapFactory.decodeResource(getResources(), R.drawable.bg);
		background2 = BitmapFactory.decodeResource(getResources(), R.drawable.bg_02);
		missile_bt = BitmapFactory.decodeResource(getResources(), R.drawable.missile_bt);
		scalex = screen_width / background.getWidth();
		scaley = screen_height / background.getHeight();
		play_bt_w = playButton.getWidth();
		play_bt_h = playButton.getHeight()/2;
		bg_y = 0;
		bg_y2 = bg_y - screen_height;
		missile_bt_y = screen_height - 10 - missile_bt.getHeight();
	}
	//初始化游戏对象
	public void initObject(){
		for(EnemyPlane obj:enemyPlanes){	
			//初始化小鱼
			if(obj instanceof SmallPlane){
				if(!obj.isAlive()){
					obj.initial(speedTime,0,0);
					break;
				}	
			}
			//初始化中鱼
			else if(obj instanceof MiddlePlane){
				if(!obj.isAlive()){
						obj.initial(speedTime,0,0);
						break;
				}
			}
			//初始化大鱼
			else if(obj instanceof BigPlane){
					if(!obj.isAlive()){
						obj.initial(speedTime,0,0);
						break;
					}
			}
			//初始化BOSS敌机
			else{
				if(bossPlaneScore >= 80000){
					if(!obj.isAlive()){
						obj.initial(0,0,0);
						break;
					}
				}
			}
		}
		//提升等级
		if(sumScore >= speedTime*100000 && speedTime < 6){
			speedTime++;	
		}
	}
	// 释放图片资源的方法
	@Override
	public void release() {
		// TODO Auto-generated method stub
		for(GameObject obj:enemyPlanes){			
			obj.release();
		}
		myPlane.release();
		if(!playButton.isRecycled()){
			playButton.recycle();
		}
		if(!background.isRecycled()){
			background.recycle();
		}
		if(!background2.isRecycled()){
			background2.recycle();
		}
		if(!missile_bt.isRecycled()){
			missile_bt.recycle();
		}
	}
	// 绘图方法
	@Override
	public void drawSelf() {
		// TODO Auto-generated method stub
		try {
			canvas = sfh.lockCanvas();
			canvas.drawColor(Color.BLACK); // 绘制背景色
			canvas.save();
			// 计算背景图片与屏幕的比例
			canvas.scale(scalex, scaley, 0, 0);
			canvas.drawBitmap(background, 0, bg_y, paint);   // 绘制背景图
			canvas.drawBitmap(background, 0, bg_y2, paint); // 绘制背景图
			canvas.restore();	
			//绘制按钮
			canvas.save();
			canvas.clipRect(10, 10, 10 + play_bt_w,10 + play_bt_h);
			if(isPlay){
				canvas.drawBitmap(playButton, 10, 10, paint);			 
			}
			else{
				canvas.drawBitmap(playButton, 10, 10 - play_bt_h, paint);
			}
			canvas.restore();

			//绘制敌机
			for(EnemyPlane obj:enemyPlanes){		
				if(obj.isAlive()){
					obj.drawSelf(canvas);					
					//检测敌机是否与玩家的飞机碰撞					
					if(obj.isCanCollide() && myPlane.isAlive()){		
						if(obj.isCollide(myPlane) && sumScore< obj.getScore()){
							myPlane.setAlive(false);
						}
						else if(obj.isCollide(myPlane) && sumScore> obj.getScore()){
							sumScore+=obj.getScore();
							obj.setAlive(false);
						}
					}
				}	
			}

			if(!myPlane.isAlive()){
				threadFlag = false;
				sounds.playSound(4, 0);			//飞机炸毁的音效
			}
			myPlane.drawSelf(canvas);	//绘制玩家的飞机

			//绘制积分文字
			paint.setTextSize(30);
			paint.setColor(Color.rgb(235, 161, 1));
			canvas.drawText("积分:"+String.valueOf(sumScore), 30 + play_bt_w, 40, paint);		//绘制文字
			//canvas.drawText("等级 X "+String.valueOf(speedTime), screen_width - 150, 40, paint); //绘制文字
		} catch (Exception err) {
			err.printStackTrace();
		} finally {
			if (canvas != null)
				sfh.unlockCanvasAndPost(canvas);
		}
	}

	// 增加游戏分数的方法 
	public void addGameScore(int score){
		middlePlaneScore += score;	// 中型敌机的积分
		bigPlaneScore += score;		// 大型敌机的积分
		bossPlaneScore += score;	// boss型敌机的积分	
		sumScore += score;			// 游戏总得分
	}
	// 播放音效
	public void playSound(int key){
		sounds.playSound(key, 0);
	}
	// 线程运行的方法
	@Override
	public void run() {
		// TODO Auto-generated method stub
		while (threadFlag) {	
			long startTime = System.currentTimeMillis();
			initObject();
			drawSelf();
			long endTime = System.currentTimeMillis();	
			if(!isPlay){
				synchronized (thread) {  
				    try {  
				    	thread.wait();  
				    } catch (InterruptedException e) {  
				        e.printStackTrace();  
				    }  
				}  		
			}	
			try {
				if (endTime - startTime < 100)
					Thread.sleep(100 - (endTime - startTime));
			} catch (InterruptedException err) {
				err.printStackTrace();
			}
		}
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Message message = new Message();
		message.what = 	ConstantUtil.TO_END_VIEW;
		message.arg1 = Integer.valueOf(sumScore);
		mainActivity.getHandler().sendMessage(message);
	}
}
