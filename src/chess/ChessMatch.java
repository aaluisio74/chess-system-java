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
	private boolean check; // Por padrão é false.
	
	private List<Piece> piecesOnTheBoard = new ArrayList<>(); //Garante que a lista seja automaticamente instanciada ao carregar a partida.
	//private List<ChessPiece> piecesOnTheBoard; //Poderia ficar assim!
	private List<Piece> capturedPieces = new ArrayList<>();

	//Construtor não foi atualizado com a instância do ArrayList<>();
	public ChessMatch() {
		board = new Board(8, 8);
		turn = 1;
		currentPlayer = Color.WHITE;
		//check = false; //Pode ficar assim! Decisão didática.
		//piecesOnTheBoard = new ArrayList<>(); //Poderia ficar assim!
		initialSetup();
	}
	
	//Somente o GET para que os atributos não sejam alterados.
	public int getTurn() {
		return turn;
	}
	
	public Color getCurrentPlayer() {
		return currentPlayer;
	}
	
	public boolean getCheck() {
		return check;
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
		
		// Se essa condição falhar, o jogador não se colocou em xeque
		if (testCheck(currentPlayer)) {
			undoMove(source, target, capturePiece);
			throw new ChessException("You can't put yourself in check!");
		}
		
		//Se o oponente se colocou em xeque, precisa ser informado.
		check = (testCheck(opponent(currentPlayer))) ? true : false;
		
		nextTurn();
		return (ChessPiece)capturePiece;
	}

	private Piece makeMove(Position source, Position target) {
		Piece p = board.removePiece(source); //Remove a peça da posição de origem.
		Piece capturedPiece = board.removePiece(target); //Remove a peça da posição de destino.
		board.placePiece(p, target); //Peça (p) na posição de destino (target).
		
		//Quando um movimento capturar um peça, 
		if (capturedPiece != null) {
			piecesOnTheBoard.remove(capturedPiece);
			capturedPieces.add(capturedPiece);
		}
		
		return capturedPiece; //Retorna a peça capturada.
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
	
	//Validação da peça de origem com duas verificações.
	private void validateSourcePosition(Position position) {
		if (!board.thereIsAPiece(position)) {
			throw new ChessException("There is no piece on source position!");
		}
		/*Terceira verificação: Se a peça atual for do adversário, não poderá ser
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
	
	//Varre o tabuleiro procurando uma peça que possa ameaçar o REI.
	private boolean testCheck(Color color) {
		//Pega a posição do REI no formato de matriz
		Position kingPosition = king(color).getChessPosition().toPosition();
		//Lista de peças do oponente do REI.
		List<Piece> opponentPieces = piecesOnTheBoard.stream().filter(x -> ((ChessPiece)x).getColor() == opponent(color)).collect(Collectors.toList());
		
		for (Piece p : opponentPieces) {
			boolean[][] mat = p.possibleMoves();
			if (mat[kingPosition.getRow()][kingPosition.getColumn()]) {
				return true;
			}
		}
		return false;
	}
	
	private void placeNewPiece(char column, int row, ChessPiece piece) {
		board.placePiece(piece, new ChessPosition(column, row).toPosition()); //Além de colocar a peça no tabuleiro...
		//...já coloca também na lista de peças do tabuleiro.
		piecesOnTheBoard.add(piece);
	}
	
	private void initialSetup() {
		placeNewPiece('c', 1, new Rook(board, Color.WHITE));
        placeNewPiece('c', 2, new Rook(board, Color.WHITE));
        placeNewPiece('d', 2, new Rook(board, Color.WHITE));
        placeNewPiece('e', 2, new Rook(board, Color.WHITE));
        placeNewPiece('e', 1, new Rook(board, Color.WHITE));
        placeNewPiece('d', 1, new King(board, Color.WHITE));

        placeNewPiece('c', 7, new Rook(board, Color.BLACK));
        placeNewPiece('c', 8, new Rook(board, Color.BLACK));
        placeNewPiece('d', 7, new Rook(board, Color.BLACK));
        placeNewPiece('e', 7, new Rook(board, Color.BLACK));
        placeNewPiece('e', 8, new Rook(board, Color.BLACK));
        placeNewPiece('d', 8, new King(board, Color.BLACK));
	}
}
