package chess;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import boardgame.Board;
import boardgame.Piece;
import boardgame.Position;
import chess.pieces.King;
import chess.pieces.Rook;

public class ChessMatch {

	private int turn;
	private Color currentPlayer;
	private Board board;
	private boolean check; // Por padr�o � false.
	private boolean checkMate;
	
	private List<Piece> piecesOnTheBoard = new ArrayList<>(); //Garante que a lista seja automaticamente instanciada ao carregar a partida.
	//private List<ChessPiece> piecesOnTheBoard; //Poderia ficar assim!
	private List<Piece> capturedPieces = new ArrayList<>();

	//Construtor n�o foi atualizado com a inst�ncia do ArrayList<>();
	public ChessMatch() {
		board = new Board(8, 8);
		turn = 1;
		currentPlayer = Color.WHITE;
		//check = false; //Pode ficar assim! Decis�o did�tica.
		//piecesOnTheBoard = new ArrayList<>(); //Poderia ficar assim!
		initialSetup();
	}
	
	//Somente o GET para que os atributos n�o sejam alterados.
	public int getTurn() {
		return turn;
	}
	
	public Color getCurrentPlayer() {
		return currentPlayer;
	}
	
	public boolean getCheck() {
		return check;
	}
	
	public boolean getCheckMate() {
		return checkMate;
	}

	public ChessPiece[][] getPieces() {
		ChessPiece[][] mat = new ChessPiece[board.getRows()][board.getColumns()];
		for (int i=0; i<board.getRows(); i++) {
			for (int j=0; j<board.getColumns(); j++) {
				mat[i][j] = (ChessPiece) board.piece(i, j);
			}
		}
		return mat;
	}
	
	public boolean[][] possibleMoves(ChessPosition sourcePosition){
		Position position = sourcePosition.toPosition();
		validateSourcePosition(position);
		return board.piece(position).possibleMoves();
	}
	
	public ChessPiece performChessMove(ChessPosition sourcePosition, ChessPosition targetPosition) {
		Position source = sourcePosition.toPosition();
		Position target = targetPosition.toPosition();
		validateSourcePosition(source);
		validateTargetPosition(source, target);
		Piece capturePiece = makeMove(source, target);
		
		// Se essa condi��o falhar, o jogador n�o se colocou em xeque
		if (testCheck(currentPlayer)) {
			undoMove(source, target, capturePiece);
			throw new ChessException("You can't put yourself in check!");
		}
		
		//Se o oponente se colocou em xeque, precisa ser informado.
		check = (testCheck(opponent(currentPlayer))) ? true : false;
		
		if (testCheckMate(opponent(currentPlayer))) { //Se a jogada feita deixou em xeque-mate, checkMate recebe TRUE.
			checkMate = true;
		}
		else {
			nextTurn(); //Pr�ximo turno.
		}
		
		return (ChessPiece)capturePiece;
	}

	private Piece makeMove(Position source, Position target) {
		Piece p = board.removePiece(source); //Remove a pe�a da posi��o de origem.
		Piece capturedPiece = board.removePiece(target); //Remove a pe�a da posi��o de destino.
		board.placePiece(p, target); //Pe�a (p) na posi��o de destino (target).
		
		//Quando um movimento capturar um pe�a, 
		if (capturedPiece != null) {
			piecesOnTheBoard.remove(capturedPiece);
			capturedPieces.add(capturedPiece);
		}
		
		return capturedPiece; //Retorna a pe�a capturada.
	}
	
	private void undoMove(Position source, Position target, Piece capturedPiece) {
		Piece p = board.removePiece(target);
		board.placePiece(p, source);
		
		if (capturedPiece != null) {
			board.placePiece(capturedPiece, target);
			capturedPieces.remove(capturedPiece);
			piecesOnTheBoard.add(capturedPiece);
		}		
	}
	
	//Valida��o da pe�a de origem com duas verifica��es.
	private void validateSourcePosition(Position position) {
		if (!board.thereIsAPiece(position)) {
			throw new ChessException("There is no piece on source position!");
		}
		/*Terceira verifica��o: Se a pe�a atual for do advers�rio, n�o poder� ser
		  movida.*/
		if (currentPlayer != ((ChessPiece)board.piece(position)).getColor()) {
			throw new ChessException("The chosen piece is not yours!");
		}
		
		if (!board.piece(position).isThereAnyPossibleMove()) {
			throw new ChessException("There is no possible moves for the chosen piece!");
		}
	}
	
	private void validateTargetPosition(Position source, Position target) {
		if (!board.piece(source).possibleMove(target)) {
			throw new ChessException("The chosen piece can't move to target position!");
		}		
	}
	
	//Incrementar o turno. E mudar o jogador atual na troca de turno.
	private void nextTurn() {
		turn++;
		currentPlayer = (currentPlayer == Color.WHITE) ? Color.BLACK : Color.WHITE;
	}
	
	private Color opponent(Color color) {
		return (color == Color.WHITE) ? Color.BLACK : Color.WHITE;
	}
	
	private ChessPiece king(Color color) {
		List<Piece> list = piecesOnTheBoard.stream().filter(x -> ((ChessPiece)x).getColor() == color).collect(Collectors.toList());
		
		for (Piece p : list) {
			if (p instanceof King) {
				return (ChessPiece)p;
			}
		}
		throw new IllegalStateException("There is no " + color + " king on the board!");
	}
	
	//Varre o tabuleiro procurando uma pe�a que possa amea�ar o REI.
	private boolean testCheck(Color color) {
		//Pega a posi��o do REI no formato de matriz
		Position kingPosition = king(color).getChessPosition().toPosition();
		//Lista de pe�as do oponente do REI.
		List<Piece> opponentPieces = piecesOnTheBoard.stream().filter(x -> ((ChessPiece)x).getColor() == opponent(color)).collect(Collectors.toList());
		
		for (Piece p : opponentPieces) {
			boolean[][] mat = p.possibleMoves();
			if (mat[kingPosition.getRow()][kingPosition.getColumn()]) {
				return true;
			}
		}
		return false;
	}
	
	private boolean testCheckMate(Color color) {
		//Se a cor que vier por par�metro n�o estiver em xeque, retorna FALSE.
		if (!testCheck(color)) {
			return false;
		}
		//Todas as pe�as da cor do par�metro ser�o filtradas nessa lista.
		List<Piece> list = piecesOnTheBoard.stream().filter(x -> ((ChessPiece)x).getColor() == color).collect(Collectors.toList());
		
		for (Piece p : list) { //Percorre todas as pe�as que est�o na lista.
			boolean[][] mat = p.possibleMoves(); //Essa armazena os movimentos poss�veis.
			for (int i = 0; i < board.getRows(); i++) { // Percorre as linhas da matriz.
				for (int j = 0; j < board.getColumns(); j++) { //Percorre as colunas da matriz.
					if (mat[i][j]) { //Se encontrar um movimento poss�vel...
						Position source = ((ChessPiece)p).getChessPosition().toPosition(); // Move a pe�a "p" para o movimento poss�vel.
						Position target = new Position(i, j); //Posi��o de destino.
						Piece capturedPiece = makeMove(source, target); //Pe�a capturada � substitu�da pela pe�a captora. Esse movimento foi para testar.
						boolean testCheck = testCheck(color); //Testa se o REI da cor do par�metro est� em cheque
						undoMove(source, target, capturedPiece); //Desfaz o movimento.
						if (!testCheck) { //Se n�o est� em xeque, retorna FALSE, pois n�o est� em xeque-mate.
							return false;
						}
					}					
				}
			}
		}
		return true; // Depois que executar o FOR e n�o for encontrado nenhum movimento que saia do check, retorna true.
	}
	
	private void placeNewPiece(char column, int row, ChessPiece piece) {
		board.placePiece(piece, new ChessPosition(column, row).toPosition()); //Al�m de colocar a pe�a no tabuleiro...
		//...j� coloca tamb�m na lista de pe�as do tabuleiro.
		piecesOnTheBoard.add(piece);
	}
	
	private void initialSetup() {
		placeNewPiece('h', 7, new Rook(board, Color.WHITE));
        placeNewPiece('d', 1, new Rook(board, Color.WHITE));
        placeNewPiece('e', 1, new King(board, Color.WHITE));

        placeNewPiece('b', 8, new Rook(board, Color.BLACK));
        placeNewPiece('a', 8, new King(board, Color.BLACK));
	}
}
