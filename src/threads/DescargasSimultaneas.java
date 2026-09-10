/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package threads;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/**
 *
 * @author Ian Suazo Palao
 */
public class DescargasSimultaneas extends JFrame{
    private JLabel lblarch1;
    private JLabel lblarch2;
    private JLabel lblarch3;
    private JProgressBar bararch1;
    private JProgressBar bararch2;
    private JProgressBar bararch3;
    private JButton btninicio;
    private JButton btncancel;
    private JTextArea txtbitacora;
    private JScrollPane scroll;
    
    private volatile boolean cancelado=false;
    private int dsc_comp=0;
    private final Object lock=new Object();
    
    public DescargasSimultaneas(){
        super("Descarga simultanea");
        init();
    }
    
    private void init(){
        setSize(450,480);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);
        
        lblarch1=new JLabel("Archivo 1:");
        lblarch2=new JLabel("Archivo 2:");
        lblarch3=new JLabel("Archivo 3:");
        
        bararch1=new JProgressBar(0,100);
        bararch2=new JProgressBar(0,100);
        bararch3=new JProgressBar(0,100);
        
        bararch1.setStringPainted(true);
        bararch2.setStringPainted(true);
        bararch3.setStringPainted(true);

        btninicio=new JButton("Iniciar descargas");
        btncancel=new JButton("Cancelar");
        txtbitacora=new JTextArea();
        txtbitacora.setEditable(false);
        scroll=new JScrollPane(txtbitacora);
        
        lblarch1.setBounds(30,20,100,25);
        bararch1.setBounds(30,45,370,25);
        lblarch2.setBounds(30,80,100,25);
        bararch2.setBounds(30,105,370,25);
        lblarch3.setBounds(30,140,100,25);
        bararch3.setBounds(30,165,370,25);
        btninicio.setBounds(50,210,150,30);
        btncancel.setBounds(230,210,150,30);
        scroll.setBounds(30,260,370,160);
        
        add(lblarch1);
        add(bararch1);
        add(lblarch2);
        add(bararch2);
        add(lblarch3);
        add(bararch3);
        add(btninicio);
        add(btncancel);
        add(scroll);
        
        btninicio.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                iniciarDescargas();
            }
        });
        
        btncancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                cancelarDescargas();
            }
        });
        
    }
    
    private class Descargas implements Runnable{
        private String nombre;
        private JProgressBar bar;
        private Random r;

        public Descargas(String nombre, JProgressBar barra){
            this.nombre= nombre;
            this.bar= barra;
            this.r= new Random();
        }

        public void run(){
            int prog=0;

            while (prog<100 && !cancelado){
                try {
                    int tiempo=r.nextInt(300)+100;
                    Thread.sleep(tiempo);
                    prog+=r.nextInt(15)+5;
                    
                    if (prog>100){
                        prog=100;
                    }

                    final int prog_act=prog;
                    SwingUtilities.invokeLater(new Runnable(){
                        public void run(){
                            bar.setValue(prog_act);
                            txtbitacora.append(nombre+": descarga esta al "+prog_act+"% de finalizacion\n");
                        }
                    });

                } 
                catch (InterruptedException e){
                    break;
                }
            }
            if (!cancelado && prog>=100){
                verificarFinDeDescarga(nombre);
            }
        }
    }
    
    private void iniciarDescargas(){
        cancelado=false;
        dsc_comp=0;
        
        bararch1.setValue(0);
        bararch2.setValue(0);
        bararch3.setValue(0);
        txtbitacora.setText("");
        btninicio.setEnabled(false);
        txtbitacora.append("Iniciando sus descargas...\n");
        
        
        Descargas a=new Descargas("Archivo 1",bararch1);
        Descargas b=new Descargas("Archivo 2",bararch2);
        Descargas c=new Descargas("Archivo 3",bararch3);
        
        Thread ta=new Thread(a);
        Thread tb=new Thread(b);
        Thread tc=new Thread(c);
        
        ta.start();
        tb.start();
        tc.start();
    }
    
    private void cancelarDescargas(){
        if(!cancelado){
            cancelado=true;
            txtbitacora.append("Sus descargas han sido canceladas.\n");
            btninicio.setEnabled(true);
        }
    }
    
    private void verificarFinDeDescarga(String nombrearch){
        synchronized(lock){
            dsc_comp++;
            
            SwingUtilities.invokeLater(new Runnable() {
                public void run(){
                    txtbitacora.append(nombrearch+": Este archivo ha sido descargado.\n");
                }
            });
            
            if(dsc_comp==3 && !cancelado){
                SwingUtilities.invokeLater(new Runnable() {
                    public void run(){
                        txtbitacora.append("Todas sus descargas han sido finalizadas.\n");
                        btninicio.setEnabled(true);
                    }
                });
            }
        }
    }
    
    public static void main(String []args){
        SwingUtilities.invokeLater(new Runnable() {
            public void run(){
                new DescargasSimultaneas().setVisible(true);
            }
        });
    }
}
