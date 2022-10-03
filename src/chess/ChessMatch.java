package chess;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import boardgame.Board;
import boardgame.Piece;
import boardgame.Position;
import chess.pieces.Bishop;
import chess.pieces.King;
import chess.pieces.Knight;
import chess.pieces.Pawn;
import chess.pieces.Queen;
import chess.pieces.Rook;

public class ChessMatch {

	private int turn;
	private Color currentPlayer;
	private Board board;
	private boolean check; // Por padrão é false.
	private boolean checkMate;

	private List<Piece> piecesOnTheBoard = new ArrayList<>(); // Garante que a lista seja automaticamente instanciada ao
																// carregar a partida.
	// private List<ChessPiece> piecesOnTheBoard; //Poderia ficar assim!
	private List<Piece> capturedPieces = new ArrayList<>();

	// Construtor não foi atualizado com a instância do ArrayList<>();
	public ChessMatch() {
		board = new Board(8, 8);
		turn = 1;
		currentPlayer = Color.WHITE;
		// check = false; //Pode ficar assim! Decisão didática.
		// piecesOnTheBoard = new ArrayList<>(); //Poderia ficar assim!
		initialSetup();
	}

	// Somente o GET para que os atributos não sejam alterados.
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
		for (int i = 0; i < board.getRows(); i++) {
			for (int j = 0; j < board.getColumns(); j++) {
				mat[i][j] = (ChessPiece) board.piece(i, j);
			}
		}
		return mat;
	}

	public boolean[][] possibleMoves(ChessPosition sourcePosition) {
		Position position = sourcePosition.toPosition();
		validateSourcePosition(position);
		return board.piece(position).possibleMoves();
	}

	public ChessPiece performChessMove(ChessPosition sourcePosition, ChessPosition targetPosition) {
		Position source = sourcePosition.toPosition();
		Position target = targetPosition.toPosition();
		validateSourcePosition(source);
		validateTargetPosition(source, target);
		Piece capturedPiece = makeMove(source, target); //and ChessMatch bugfix: Faltou a letra "d" em capturedPiece.

		// Se essa condição falhar, o jogador não se colocou em xeque
		if (testCheck(currentPlayer)) {
			undoMove(source, target, capturedPiece); //and ChessMatch bugfix: Faltou a letra "d" em capturedPiece.
			throw new ChessException("You can't put yourself in check!");
		}

		// Se o oponente se colocou em xeque, precisa ser informado.
		check = (testCheck(opponent(currentPlayer))) ? true : false;

		if (testCheckMate(opponent(currentPlayer))) { // Se a jogada feita deixou em xeque-mate, checkMate recebe TRUE.
			checkMate = true;
		} else {
			nextTurn(); // Próximo turno.
		}

		return (ChessPiece)capturedPiece; //and ChessMatch bugfix: Faltou a letra "d" em capturedPiece.
	}

	private Piece makeMove(Position source, Position target) {
		// Remove a peça da posição de origem. Aqui é feito um downcasting
		ChessPiece p = (ChessPiece) board.removePiece(source);
		// Quando mover a peça, é feito um incremento na contagem dos movimentos da
		// peça.
		p.increaseMoveCount(); // Só é possível chamar esse metódo através da classe ChessPeace.
		Piece capturedPiece = board.removePiece(target); // Remove a peça da posição de destino.

		// Esse 'p' recebe um Piece e faz o Upcasting naturalmente.
		board.placePiece(p, target); // Peça (p) na posição de destino (target).

		// Quando um movimento capturar um peça,
		if (capturedPiece != null) {
			piecesOnTheBoard.remove(capturedPiece);
			capturedPieces.add(capturedPiece);
		}

		// #specialmove castling kingside rook (ROQUE PEQUENO)
		if (p instanceof King && target.getColumn() == source.getColumn() + 2) {
			// Posição de origem da TORRE.
			Position sourceT = new Position(source.getRow(), source.getColumn() + 3);
			// Posição de destino da TORRE.
			Position targetT = new Position(source.getRow(), source.getColumn() + 1);
			// Para pegar a TORRE na sua origem para mover
			ChessPiece rook = (ChessPiece) board.removePiece(sourceT);
			// Coloca a TORRE na posição de destino.
			board.placePiece(rook, targetT);
			// Incrementa a quantidade de movimentos da TORRE
			rook.increaseMoveCount();
		}

		// #specialmove castling queenside rook (ROQUE GRANDE)
		if (p instanceof King && target.getColumn() == source.getColumn() - 2) {
			// Posição de origem da TORRE.
			Position sourceT = new Position(source.getRow(), source.getColumn() - 4);
			// Posição de destino da TORRE.
			Position targetT = new Position(source.getRow(), source.getColumn() - 1);
			// Para pegar a TORRE na sua origem para mover
			ChessPiece rook = (ChessPiece)board.removePiece(sourceT);
			// Coloca a TORRE na posição de destino.
			board.placePiece(rook, targetT);
			// Incrementa a quantidade de movimentos da TORRE
			rook.increaseMoveCount();
		}

		return capturedPiece; // Retorna a peça capturada.
	}

	private void undoMove(Position source, Position target, Piece capturedPiece) {
		ChessPiece p = (ChessPiece) board.removePiece(target);
		// Quando mover a peça, é feito um decremento na contagem dos movimentos.
		p.decreaseMoveCount(); // Só é possível chamar esse metódo através da classe ChessPeace.
		board.placePiece(p, source);

		if (capturedPiece != null) {
			board.placePiece(capturedPiece, target);
			capturedPieces.remove(capturedPiece);
			piecesOnTheBoard.add(capturedPiece);
		}

		// #specialmove castling kingside rook (ROQUE PEQUENO)
		if (p instanceof King && target.getColumn() == source.getColumn() + 2) {
			// Posição de origem da TORRE.
			Position sourceT = new Position(source.getRow(), source.getColumn() + 3);
			// Posição de destino da TORRE.
			Position targetT = new Position(source.getRow(), source.getColumn() + 1);
			// Para pegar a TORRE na sua origem para mover
			ChessPiece rook = (ChessPiece) board.removePiece(targetT);
			// Coloca a TORRE na posição de destino.
			board.placePiece(rook, sourceT);
			// Incrementa a quantidade de movimentos da TORRE
			rook.decreaseMoveCount();
		}

		// #specialmove castling queenside rook (ROQUE GRANDE)
		if (p instanceof King && target.getColumn() == source.getColumn() - 2) {
			// Posição de origem da TORRE.
			Position sourceT = new Position(source.getRow(), source.getColumn() - 4);
			// Posição de destino da TORRE.
			Position targetT = new Position(source.getRow(), source.getColumn() + 1);
			// Para pegar a TORRE na sua origem para mover
			ChessPiece rook = (ChessPiece) board.removePiece(targetT);
			// Coloca a TORRE na posição de destino.
			board.placePiece(rook, sourceT);
			// Incrementa a quantidade de movimentos da TORRE
			rook.decreaseMoveCount();

		}

	}

	// Validação da peça de origem com duas verificações.
	private void validateSourcePosition(Position position) {
		if (!board.thereIsAPiece(position)) {
			throw new ChessException("There is no piece on source position!");
		}
		/*
		 * Terceira verificação: Se a peça atual for do adversário, não poderá ser
		 * movida.
		 */
		if (currentPlayer != ((ChessPiece) board.piece(position)).getColor()) {
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

	// Incrementar o turno. E mudar o jogador atual na troca de turno.
	private void nextTurn() {
		turn++;
		currentPlayer = (currentPlayer == Color.WHITE) ? Color.BLACK : Color.WHITE;
	}

	private Color opponent(Color color) {
		return (color == Color.WHITE) ? Color.BLACK : Color.WHITE;
	}

	private ChessPiece king(Color color) {
		List<Piece> list = piecesOnTheBoard.stream().filter(x -> ((ChessPiece) x).getColor() == color)
				.collect(Collectors.toList());

		for (Piece p : list) {
			if (p instanceof King) {
				return (ChessPiece) p;
			}
		}
		throw new IllegalStateException("There is no " + color + " king on the board!");
	}

	// Varre o tabuleiro procurando uma peça que possa ameaçar o REI.
	private boolean testCheck(Color color) {
		// Pega a posição do REI no formato de matriz
		Position kingPosition = king(color).getChessPosition().toPosition();
		// Lista de peças do oponente do REI.
		List<Piece> opponentPieces = piecesOnTheBoard.stream()
				.filter(x -> ((ChessPiece) x).getColor() == opponent(color)).collect(Collectors.toList());

		for (Piece p : opponentPieces) {
			boolean[][] mat = p.possibleMoves();
			if (mat[kingPosition.getRow()][kingPosition.getColumn()]) {
				return true;
			}
		}
		return false;
	}

	private boolean testCheckMate(Color color) {
		// Se a cor que vier por parâmetro não estiver em xeque, retorna FALSE.
		if (!testCheck(color)) {
			return false;
		}
		// Todas as peças da cor do parâmetro serão filtradas nessa lista.
		List<Piece> list = piecesOnTheBoard.stream().filter(x -> ((ChessPiece) x).getColor() == color)
				.collect(Collectors.toList());

		for (Piece p : list) { // Percorre todas as peças que estão na lista.
			boolean[][] mat = p.possibleMoves(); // Essa armazena os movimentos possíveis.
			for (int i = 0; i < board.getRows(); i++) { // Percorre as linhas da matriz.
				for (int j = 0; j < board.getColumns(); j++) { // Percorre as colunas da matriz.
					if (mat[i][j]) { // Se encontrar um movimento possível...
						Position source = ((ChessPiece) p).getChessPosition().toPosition(); // Move a peça "p" para o
																							// movimento possível.
						Position target = new Position(i, j); // Posição de destino.
						Piece capturedPiece = makeMove(source, target); // Peça capturada é substituída pela peça
																		// captora. Esse movimento foi para testar.
						boolean testCheck = testCheck(color); // Testa se o REI da cor do parâmetro está em cheque
						undoMove(source, target, capturedPiece); // Desfaz o movimento.
						if (!testCheck) { // Se não está em xeque, retorna FALSE, pois não está em xeque-mate.
							return false;
						}
					}
				}
			}
		}
		return true; // Depois que executar o FOR e não for encontrado nenhum movimento que saia do
						// check, retorna true.
	}

	private void placeNewPiece(char column, int row, ChessPiece piece) {
		board.placePiece(piece, new ChessPosition(column, row).toPosition()); // Além de colocar a peça no tabuleiro...
		// ...já coloca também na lista de peças do tabuleiro.
		piecesOnTheBoard.add(piece);
	}

	private void initialSetup() {
		placeNewPiece('a', 1, new Rook(board, Color.WHITE));
		placeNewPiece('b', 1, new Knight(board, Color.WHITE));
		placeNewPiece('c', 1, new Bishop(board, Color.WHITE));
		placeNewPiece('d', 1, new Queen(board, Color.WHITE));
		placeNewPiece('e', 1, new King(board, Color.WHITE, this));
		placeNewPiece('f', 1, new Bishop(board, Color.WHITE));
		placeNewPiece('g', 1, new Knight(board, Color.WHITE));
		placeNewPiece('h', 1, new Rook(board, Color.WHITE));
		placeNewPiece('a', 2, new Pawn(board, Color.WHITE));
		placeNewPiece('b', 2, new Pawn(board, Color.WHITE));
		placeNewPiece('c', 2, new Pawn(board, Color.WHITE));
		placeNewPiece('d', 2, new Pawn(board, Color.WHITE));
		placeNewPiece('e', 2, new Pawn(board, Color.WHITE));
		placeNewPiece('f', 2, new Pawn(board, Color.WHITE));
		placeNewPiece('g', 2, new Pawn(board, Color.WHITE));
		placeNewPiece('h', 2, new Pawn(board, Color.WHITE));

		placeNewPiece('a', 8, new Rook(board, Color.BLACK));
		placeNewPiece('b', 8, new Knight(board, Color.BLACK));
		placeNewPiece('c', 8, new Bishop(board, Color.BLACK));
		placeNewPiece('d', 8, new Queen(board, Color.BLACK));
		placeNewPiece('e', 8, new King(board, Color.BLACK, this));
		placeNewPiece('f', 8, new Bishop(board, Color.BLACK));
		placeNewPiece('g', 8, new Knight(board, Color.BLACK));
		placeNewPiece('h', 8, new Rook(board, Color.BLACK));
		placeNewPiece('a', 7, new Pawn(board, Color.BLACK));
		placeNewPiece('b', 7, new Pawn(board, Color.BLACK));
		placeNewPiece('c', 7, new Pawn(board, Color.BLACK));
		placeNewPiece('d', 7, new Pawn(board, Color.BLACK));
		placeNewPiece('e', 7, new Pawn(board, Color.BLACK));
		placeNewPiece('f', 7, new Pawn(board, Color.BLACK));
		placeNewPiece('g', 7, new Pawn(board, Color.BLACK));
		placeNewPiece('h', 7, new Pawn(board, Color.BLACK));
	}
}
