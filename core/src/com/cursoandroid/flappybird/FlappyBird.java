package com.cursoandroid.flappybird;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.sun.org.apache.xpath.internal.operations.Bool;
import com.sun.org.apache.xpath.internal.operations.String;

import java.util.Random;

public class FlappyBird extends ApplicationAdapter {

	private SpriteBatch batch;
	private Texture[] passaro;
	private Texture fundo;
	private Texture canoBaixo;
	private Texture canoTopo;
	private Texture gameOver;
	private Random numeroRandomico;
	private int alturaCanosRandomica;
	private int estadoJogo = 0; //0- jogo parado, 1- jogo rodando, 2-game over
	private BitmapFont fonte;
	private BitmapFont mensagem;
	private int pontuacao = 0;
	private boolean marcouPonto = false;

	//colisões
	private Circle passaroCirculo;
	private Rectangle canoTopoRetangulo;
	private Rectangle canoBaixoRetangulo;
	//private ShapeRenderer shape;

	//Atributos de configuração
	private float alturaDispositivo;
	private float larguraDispositivo;

	private float variacaoImagensPassaro = 0;
	private float velocidadeQuedaPassaro = 0;
	private float posicaoInicialVertical;
	private float posicaoMovimentoCanoHorizintal;
	private float espacoEntreCanos;
	private float deltaTime; // tempo entre cada chamada do metodo render

	//camera
	private OrthographicCamera camera;
	private Viewport viewport;
	private final float VIRTUAL_WIDTH = 768;
	private final float VIRTUAL_HEIGHT = 1024;

	@Override
	public void create () {

		batch = new SpriteBatch();

		//configurando a fonte da pontuação
		fonte = new BitmapFont();
		fonte.setColor(Color.WHITE);
		fonte.getData().setScale(6);//tamanho da fonte

		numeroRandomico = new Random();

		//fonte da mensgem para clicar na tela para recomeçar o jogo
		mensagem = new BitmapFont();
		mensagem.setColor(Color.WHITE);
		mensagem.getData().setScale(3);

		//colisão
		passaroCirculo = new Circle();
		/*canoBaixoRetangulo = new Rectangle();
		canoTopoRetangulo = new Rectangle();
		shape = new ShapeRenderer();*/


		//array dos 3 frames do gif do passaro
		passaro = new Texture[3];
		passaro[0] = new Texture("passaro1.png");
		passaro[1] = new Texture("passaro2.png");
		passaro[2] = new Texture("passaro3.png");

		fundo = new Texture("fundo.png");
		canoBaixo = new Texture("cano_baixo.png");
		canoTopo = new Texture("cano_topo.png");
		gameOver = new Texture("game_over.png");

		//configurando a camera
		camera =  new OrthographicCamera();
		camera.position.set(VIRTUAL_WIDTH/2, VIRTUAL_HEIGHT/2, 0);
		viewport = new StretchViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);

		//definindo largura e altura da tela
		alturaDispositivo = VIRTUAL_HEIGHT;
		larguraDispositivo = VIRTUAL_WIDTH;
		posicaoInicialVertical = alturaDispositivo/2;
		posicaoMovimentoCanoHorizintal = larguraDispositivo;
		espacoEntreCanos = 300;


	}

	@Override
	public void render () {

		//atualizando camerra
		camera.update();

		//Limpar frames anteriores para otimizar o uso de memoria
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		//passaro bater as asas
		deltaTime = Gdx.graphics.getDeltaTime(); //getDeltaTime() pega o tempo de cada vez q chama o metodo render
		//trocar frame do passaro
		variacaoImagensPassaro += deltaTime * 5;
		if (variacaoImagensPassaro > 3) {
			variacaoImagensPassaro = 0;
		}

		//se o jogador ainda nao clicou na tela para começar o jogo
		if (estadoJogo == 0){
			if (Gdx.input.justTouched()){
				estadoJogo = 1;
			}
		}else {//se ele clicou na tela para começar o jogo

			//passaro comeca a cair
			velocidadeQuedaPassaro++;

			//verifica para o passaro nao sair da tela
			if (posicaoInicialVertical > 0 || velocidadeQuedaPassaro < 0) {
				posicaoInicialVertical -= velocidadeQuedaPassaro;
			}

			if (estadoJogo == 1) {

				// velocidade do cano
				posicaoMovimentoCanoHorizintal -= deltaTime * 200;

				//quando clicar na tela
				if (Gdx.input.justTouched()) {
					velocidadeQuedaPassaro = -20; //-20 é o tamanho do pulo do passaro
				}

				//verifica se o cano saiu da tela
				if (posicaoMovimentoCanoHorizintal < (-canoBaixo.getWidth())) {
					posicaoMovimentoCanoHorizintal = larguraDispositivo;
					alturaCanosRandomica = numeroRandomico.nextInt(400) - 200; //pega o numero aleatorio e diminui a metade para poder negativo
					marcouPonto = false; //deixa o jogadaor marcar novo ponto quano o cano volta pro começo da tela
				}

				//pontuação
				if (posicaoMovimentoCanoHorizintal < 120) { //se o cano passar de 120 que é a posição do passaro
					if (!marcouPonto) {
						pontuacao++;
						marcouPonto = true;
					}
				}
			}else{ //tela de game over

				if (Gdx.input.justTouched()){
					estadoJogo = 0;
					pontuacao = 0;
					velocidadeQuedaPassaro = 0;
					posicaoInicialVertical = alturaDispositivo/2;
					posicaoMovimentoCanoHorizintal = larguraDispositivo;

				}
			}
		}

		//configurar dados de projeção da camera
		batch.setProjectionMatrix(camera.combined);

		//exibindo elementos na tela
		batch.begin();
		batch.draw(fundo, 0, 0, larguraDispositivo, alturaDispositivo);//(Textura, X, Y, Altura da imagem, largura da imagem)
		batch.draw(canoTopo, posicaoMovimentoCanoHorizintal, (alturaDispositivo / 2) + (espacoEntreCanos / 2) + alturaCanosRandomica);
		batch.draw(canoBaixo, posicaoMovimentoCanoHorizintal, (alturaDispositivo / 2) - canoBaixo.getHeight() - (espacoEntreCanos / 2) + alturaCanosRandomica);
		batch.draw(passaro[(int) variacaoImagensPassaro], 120, posicaoInicialVertical); //textura, posicao eixo x, posicao eixo y
		fonte.draw(batch, java.lang.String.valueOf(pontuacao), larguraDispositivo / 2, alturaDispositivo - 50); //(batch, texto na tela, x, y)

		//game over
		if (estadoJogo == 2){
			batch.draw(gameOver, (larguraDispositivo/2)-(gameOver.getWidth()/2), (alturaDispositivo/2));

			//exibindo mensagem de game over
			mensagem.draw(batch, "Toque na tela para reiniciar", larguraDispositivo/7, alturaDispositivo/2 - 30);
		}
		batch.end();

		//criar formas para colisao
		passaroCirculo.set(120 + (passaro[0].getWidth() / 2), posicaoInicialVertical + (passaro[0].getHeight() / 2), (passaro[0].getWidth() / 2)); //setando posicao do circulo q vao ser feitas as colisoes

		canoBaixoRetangulo = new Rectangle(posicaoMovimentoCanoHorizintal,
				(alturaDispositivo / 2) - canoBaixo.getHeight() - (espacoEntreCanos / 2) + alturaCanosRandomica,
				canoBaixo.getWidth(),
				canoBaixo.getHeight()); //(x, y, width, height)

		canoTopoRetangulo = new Rectangle(posicaoMovimentoCanoHorizintal,
				(alturaDispositivo / 2) + (espacoEntreCanos / 2) + alturaCanosRandomica,
				canoTopo.getWidth(),
				canoTopo.getHeight());

		//desenhar formar para colisao
		/*shape.begin(ShapeRenderer.ShapeType.Filled);
		shape.circle(passaroCirculo.x, passaroCirculo.y, passaroCirculo.radius);
		shape.rect(canoBaixoRetangulo.x, canoBaixoRetangulo.y, canoBaixoRetangulo.width, canoBaixoRetangulo.height);
		shape.rect(canoTopoRetangulo.x, canoTopoRetangulo.y, canoTopoRetangulo.width, canoTopoRetangulo.height);
		shape.setColor(Color.RED);
		shape.end();*/

		//teste de colisao
		if (Intersector.overlaps(passaroCirculo, canoBaixoRetangulo) || Intersector.overlaps(passaroCirculo, canoTopoRetangulo )|| posicaoInicialVertical >= alturaDispositivo || posicaoInicialVertical <= 0){
			//Gdx.app.log("Colisão", "Houve colisão");
			estadoJogo = 2;
		}
	}

	//sobreescrevendo metodo para redimensionar de acordo com a resolução do dispositivo que estiver rodando o jogo
	@Override
	public void resize(int width, int height) {
		viewport.update(width, height);
	}
}
