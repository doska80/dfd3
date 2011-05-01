package com.bpn.diplom.gui.listeners;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JButton;

/**
 * При отпускании клавиши keyEvent выполняется клик по кнопке button.
 * Если конструктор без параметров то игнор.
 * Если в конструкторе ссылка только на button, то срабатывать по клавише KeyEvent.VK_ENTER
 * Полный конструктор получает ссылку на button и keyEvent
 * @author pavel
 *
 */
public class KeyAdapterEnterOrAnoterKey extends KeyAdapter {
	
	JButton button;
	int key;
	
	/**
	 * ЛУЧШЕ �?СПОЛЬЗОВАТЬ КОНСТРУКТОР С АРГУМЕНТАМ�?
	 * При отпускании клавиши keyEvent выполняется клик по кнопке button.
	 * Если конструктор без параметров то игнор.
	 * Если в конструкторе ссылка только на button, то срабатывать по клавише KeyEvent.VK_ENTER
	 * Полный конструктор получает ссылку на button и keyEvent
	 */
	public KeyAdapterEnterOrAnoterKey(){
		super();
	}
	
	/**
	 * При отпускании клавиши keyEvent выполняется клик по кнопке button.
	 * Если конструктор без параметров то игнор.
	 * Если в конструкторе ссылка только на button, то срабатывать по клавише KeyEvent.VK_ENTER
	 * Полный конструктор получает ссылку на button и keyEvent
	 * @param btn - ссылка на кнопку по которой будет выполнятся doClick()
	 */
	public KeyAdapterEnterOrAnoterKey(JButton btn){
		super();
		button = btn;
		key = KeyEvent.VK_ENTER;
	}
	
	/**
	 * При отпускании клавиши keyEvent выполняется клик по кнопке button.
	 * Если конструктор без параметров то игнор.
	 * Если в конструкторе ссылка только на button, то срабатывать по клавише KeyEvent.VK_ENTER
	 * Полный конструктор получает ссылку на button и keyEvent
	 * @param btn - ссылка на кнопку по которой будет выполнятся doClick()
	 * @param keyEvent - клавиша на которую реагирует листенер 
	 */
	public KeyAdapterEnterOrAnoterKey(JButton btn, int keyEvent){
		super();
		button = btn;
		key = keyEvent;
	}
	
	
	
	@Override
	public void keyPressed(KeyEvent e) {
		if(button == null || key == 0){
			return;
		}
		
		if(e.getKeyCode() == key){
			button.doClick();
		}
	}
}
