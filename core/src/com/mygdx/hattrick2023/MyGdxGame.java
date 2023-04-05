package com.mygdx.hattrick2023;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Game;

public class MyGdxGame extends Game implements ApplicationListener {

	private final com.mygdx.hattrick2023.DeviceAPI mController;

	public MyGdxGame(com.mygdx.hattrick2023.DeviceAPI mController) {
		this.mController = mController;
	}

	@Override
	public void create () {
		setScreen(new com.mygdx.hattrick2023.MainMenuScreen(this, mController));
	//	setScreen(new GameScreenOffline(this, mController));
	}

	@Override
	public void render () {
		super.render();
	}

	@Override
	public void dispose () {}
}
