package com.example.object;

import java.util.ArrayList;
import java.util.List;
import com.example.factory.GameObjectFactory;
import com.example.interfaces.IMyPlane;
import com.example.mybeatplane.R;
import com.example.view.MainView;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
/*玩家飞机的类*/
public class MyPlane extends GameObject implements IMyPlane{
	private float middle_x;			 // 飞机的中心坐标
	private float middle_y;
	private long startTime;	 	 	 // 开始的时间
	private long endTime;	 	 	 // 结束的时间
	private boolean isChangeBullet;  // 标记更换了子弹
	private Bitmap myplane;			 // 飞机飞行时的图片
	private Bitmap myplane2;		 // 飞机爆炸时的图片
	private MainView mainView;
	private GameObjectFactory factory;
	public MyPlane(Resources resources) {
		super(resources);
		// TODO Auto-generated constructor stub
		initBitmap();
		this.speed = 8;
		factory = new GameObjectFactory();
	}
	public void setMainView(MainView mainView) {
		this.mainView = mainView;
	}
	// 设置屏幕宽度和高度
	@Override
	public void setScreenWH(float screen_width, float screen_height) {
		super.setScreenWH(screen_width, screen_height);
		object_x = screen_width/2 - object_width/2;
		object_y = screen_height - object_height;
		middle_x = object_x + object_width/2;
		middle_y = object_y + object_height/2;
	}
	// 初始化图片资源的
	@Override
	public void initBitmap() {
		// TODO Auto-generated method stub
		myplane = BitmapFactory.decodeResource(resources, R.drawable.me1);
		myplane2 = BitmapFactory.decodeResource(resources, R.drawable.myplaneexplosion);
		object_width = myplane.getWidth() ; // 获得每一帧位图的宽
		object_height = myplane.getHeight(); 	// 获得每一帧位图的高
	}
	// 对象的绘图方法
	@Override
	public void drawSelf(Canvas canvas) {
		// TODO Auto-generated method stub
		if(isAlive){
			int x = (int) (currentFrame * object_width); // 获得当前帧相对于位图的X坐标
			canvas.save();
			canvas.clipRect(object_x, object_y, object_x + object_width, object_y + object_height);
			canvas.drawBitmap(myplane, object_x, object_y, paint);
			canvas.restore();
			currentFrame++;
			if (currentFrame >= 2) {
				currentFrame = 0;
			}
		}
		else{
			int x = (int) (currentFrame * object_width); // 获得当前帧相对于位图的Y坐标
			canvas.save();
			canvas.clipRect(object_x, object_y, object_x + object_width, object_y
					+ object_height);
			//canvas.drawBitmap(myplane2, object_x - x, object_y, paint);
			canvas.restore();
			currentFrame++;
			if (currentFrame >= 2) {
				currentFrame = 1;
			}
		}
	}
	// 释放资源的方法
	@Override
	public void release() {
		// TODO Auto-generated method stub
		if(!myplane.isRecycled()){
			myplane.recycle();
		}
		if(!myplane2.isRecycled()){
			myplane2.recycle();
		}
	}

	//getter和setter方法
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	@Override
	public float getMiddle_x() {
		return middle_x;
	}
	@Override
	public void setMiddle_x(float middle_x) {
		this.middle_x = middle_x;
		this.object_x = middle_x - object_width/2;
	}
	@Override
	public float getMiddle_y() {
		return middle_y;
	}
	@Override
	public void setMiddle_y(float middle_y) {
		this.middle_y = middle_y;
		this.object_y = middle_y - object_height/2;
	}	
}
