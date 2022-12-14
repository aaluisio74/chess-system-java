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
	private boolean check; // Por padr?o ? false.
	private boolean checkMate;
	private ChessPiece enPassantVulnerable; // Valor padr?o NULO (null). Portanto, n?o tem nenhuma pe?a vulner?vel para
											// o pr?ximo turno.
	private ChessPiece promoted;

	private List<Piece> piecesOnTheBoard = new ArrayList<>(); // Garante que a lista seja automaticamente instanciada ao
																// carregar a partida.
	// private List<ChessPiece> piecesOnTheBoard; //Poderia ficar assim!
	private List<Piece> capturedPieces = new ArrayList<>();

	// Construtor n?o foi atualizado com a inst?ncia do ArrayList<>();
	public ChessMatch() {
		board = new Board(8, 8);
		turn = 1;
		currentPlayer = Color.WHITE;
		// check = false; //Pode ficar assim! Decis?o did?tica.
		// piecesOnTheBoard = new ArrayList<>(); //Poderia ficar assim!
		initialSetup();
	}

	// Somente o GET para que os atributos n?o sejam alterados.
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

	public ChessPiece getEnPassantVulnerable() {
		return enPassantVulnerable;
	}
	
	public ChessPiece getPromoted() {
		return promoted;
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
		Piece capturedPiece = makeMove(source, target); // and ChessMatch bugfix: Faltou a letra "d" em capturedPiece.

		// Se essa condi??o falhar, o jogador n?o se colocou em xeque
		if (testCheck(currentPlayer)) {
			undoMove(source, target, capturedPiece); // and ChessMatch bugfix: Faltou a letra "d" em capturedPiece.
			throw new ChessException("You can't put yourself in check!");
		}

		// Pega uma refer?ncia da pe?a que se moveu!
		ChessPiece movedPiece = (ChessPiece) board.piece(target);

		// # Special move Promotion
		promoted = null;
		if (movedPiece instanceof Pawn) {
			if ((movedPiece.getColor() == Color.WHITE && target.getRow() == 0) || (movedPiece.getColor() == Color.BLACK && target.getRow() == 7)) {
				//Primeiro joga o PE?O e depois, na pr?xima linha, faz a troca.
				promoted = (ChessPiece)board.piece(target);
				//Facilita a implementa??o ao colocar a rainha como primeira op??o escolhida e depois disponibiliar as outras pe?as.
				promoted = replacePromotedPiece("Q");
			}
		}
		
		// Se o oponente se colocou em xeque, precisa ser informado.
		check = (testCheck(opponent(currentPlayer))) ? true : false;

		if (testCheckMate(opponent(currentPlayer))) { // Se a jogada feita deixou em xeque-mate, checkMate recebe TRUE.
			checkMate = true;
		} else {
			nextTurn(); // Pr?ximo turno.
		}

		// #Special move En Passant
		/*
		 * Se a pe?a movida for um pe?o e a diferen?a de casas for 2 duas casas para
		 * mais ou para menos, significa que foi movimento inicial de pe?o com duas
		 * casas. Est? vuner?vel a tomar o En Passant no pr?ximo turno.
		 */
		if (movedPiece instanceof Pawn
				&& (target.getRow() == source.getRow() - 2 || target.getRow() == source.getRow() + 2)) {
			enPassantVulnerable = movedPiece;
		} else {
			enPassantVulnerable = null;
		}

		return (ChessPiece) capturedPiece; // and ChessMatch bugfix: Faltou a letra "d" em capturedPiece.
	}
	
	// # Special move Promotion
	public ChessPiece replacePromotedPiece(String type) {
		if (promoted == null) {
			throw new IllegalStateException("There is no piece to be promoted!");
		}
		if (!type.equals("B") && !type.equals("N") && !type.equals("R") && !type.equals("Q")) {
			/*A linha abaixo foi comentada pois n?o ser? mais necess?ria. Como a RAINHA j? ? padr?o na troca da pe?a
			  promovida, caso o receba nulo, deve retornar a pe?a promovida. Outro motivo ? que essa exce??o e do pacote
			  java.security.InvalidParameterException que n?o tem nada a ver com o prop?sito desse programa. N?o h? quest?o
			  de seguran?a de sistema nesse programa de jogo de xadrez.*/
			//throw new InvalidParameterException("Invalid type for promotion!");
			
			//Pe?a promovida
			return promoted;
		}
		
		//Remove a pe?a que estava na posi??o.
		Position pos = promoted.getChessPosition().toPosition();
		Piece p = board.removePiece(pos);
		piecesOnTheBoard.remove(p);
		
		//Recebe a mesma cor da pe?o promovida.
		ChessPiece newPiece = newPiece(type, promoted.getColor());
		//Colocar a nova pe?a na posi??o da pe?a promovida.
		board.placePiece(newPiece, pos);
		//Adiciona a nova pe?a criada.
		piecesOnTheBoard.add(newPiece);
		
		return newPiece; //Retorna a nova pe?a instanciada.
		
	}
	
	// # Special move Promotion
	//M?todo auxiliar para a troca da pe?a promovida. Instancia a pe?a que ser? reposta.
	private ChessPiece newPiece(String type, Color color) {
		if (type.equals("B")) return new Bishop(board, color);
		if (type.equals("N")) return new Knight(board, color);
		if (type.equals("Q")) return new Queen(board, color);
		return new Rook(board, color);
	}

	private Piece makeMove(Position source, Position target) {
		// Remove a pe?a da posi??o de origem. Aqui ? feito um downcasting
		ChessPiece p = (ChessPiece) board.removePiece(source);
		// Quando mover a pe?a, ? feito um incremento na contagem dos movimentos da
		// pe?a.
		p.increaseMoveCount(); // S? ? poss?vel chamar esse met?do atrav?s da classe ChessPeace.
		Piece capturedPiece = board.removePiece(target); // Remove a pe?a da posi??o de destino.

		// Esse 'p' recebe um Piece e faz o Upcasting naturalmente.
		board.placePiece(p, target); // Pe?a (p) na posi??o de destino (target).

		// Quando um movimento capturar um pe?a,
		if (capturedPiece != null) {
			piecesOnTheBoard.remove(capturedPiece);
			capturedPieces.add(capturedPiece);
		}

		// #specialmove castling kingside rook (ROQUE PEQUENO)
		if (p instanceof King && target.getColumn() == source.getColumn() + 2) {
			// Posi??o de origem da TORRE.
			Position sourceT = new Position(source.getRow(), source.getColumn() + 3);
			// Posi??o de destino da TORRE.
			Position targetT = new Position(source.getRow(), source.getColumn() + 1);
			// Para pegar a TORRE na sua origem para mover
			ChessPiece rook = (ChessPiece) board.removePiece(sourceT);
			// Coloca a TORRE na posi??o de destino.
			board.placePiece(rook, targetT);
			// Incrementa a quantidade de movimentos da TORRE
			rook.increaseMoveCount();
		}

		// #specialmove castling queenside rook (ROQUE GRANDE)
		if (p instanceof King && target.getColumn() == source.getColumn() - 2) {
			// Posi??o de origem da TORRE.
			Position sourceT = new Position(source.getRow(), source.getColumn() - 4);
			// Posi??o de destino da TORRE.
			Position targetT = new Position(source.getRow(), source.getColumn() - 1);
			// Para pegar a TORRE na sua origem para mover
			ChessPiece rook = (ChessPiece) board.removePiece(sourceT);
			// Coloca a TORRE na posi??o de destino.
			board.placePiece(rook, targetT);
			// Incrementa a quantidade de movimentos da TORRE
			rook.increaseMoveCount();
		}

		// #Special move - En Passant
		/*
		 * Tratamento especial pois esse movimento foge mec?nica normal, ou seja, quando
		 * uma pe?a para em cima de uma pe?a advers?ria, ele a captura. Nesse caso, vai
		 * parar em uma casa vazia e a captura ter? que ser manual.
		 */
		// Testa se a pe?a movida ? uma inst?ncia de PE?O.
		if (p instanceof Pawn) {
			// Se a coluna de origem ? diferente da coluna de destino e a pe?a capturada
			// igual a nulo, ? um En Passant.
			if (source.getColumn() != target.getColumn() && capturedPiece == null) {
				Position pawnPosition;
				if (p.getColor() == Color.WHITE) {
					pawnPosition = new Position(target.getRow() + 1, target.getColumn());
				} else {
					pawnPosition = new Position(target.getRow() - 1, target.getColumn());
				}
				capturedPiece = board.removePiece(pawnPosition);
				capturedPieces.add(capturedPiece);
				piecesOnTheBoard.remove(capturedPiece);
			}
		}

		return capturedPiece; // Retorna a pe?a capturada.
	}

	private void undoMove(Position source, Position target, Piece capturedPiece) {
		ChessPiece p = (ChessPiece) board.removePiece(target);
		// Quando mover a pe?a, ? feito um decremento na contagem dos movimentos.
		p.decreaseMoveCount(); // S? ? poss?vel chamar esse met?do atrav?s da classe ChessPeace.
		board.placePiece(p, source);

		if (capturedPiece != null) {
			board.placePiece(capturedPiece, target);
			capturedPieces.remove(capturedPiece);
			piecesOnTheBoard.add(capturedPiece);
		}

		// #specialmove castling kingside rook (ROQUE PEQUENO)
		if (p instanceof King && target.getColumn() == source.getColumn() + 2) {
			// Posi??o de origem da TORRE.
			Position sourceT = new Position(source.getRow(), source.getColumn() + 3);
			// Posi??o de destino da TORRE.
			Position targetT = new Position(source.getRow(), source.getColumn() + 1);
			// Para pegar a TORRE na sua origem para mover
			ChessPiece rook = (ChessPiece) board.removePiece(targetT);
			// Coloca a TORRE na posi??o de destino.
			board.placePiece(rook, sourceT);
			// Incrementa a quantidade de movimentos da TORRE
			rook.decreaseMoveCount();
		}

		// #specialmove castling queenside rook (ROQUE GRANDE)
		if (p instanceof King && target.getColumn() == source.getColumn() - 2) {
			// Posi??o de origem da TORRE.
			Position sourceT = new Position(source.getRow(), source.getColumn() - 4);
			// Posi??o de destino da TORRE.
			Position targetT = new Position(source.getRow(), source.getColumn() + 1);
			// Para pegar a TORRE na sua origem para mover
			ChessPiece rook = (ChessPiece) board.removePiece(targetT);
			// Coloca a TORRE na posi??o de destino.
			board.placePiece(rook, sourceT);
			// Incrementa a quantidade de movimentos da TORRE
			rook.decreaseMoveCount();
		}

		// #Special move - En Passant
		/* Tratamento especial pois esse movimento foge mec?nica normal, ou seja, quando
		 * uma pe?a para em cima de uma pe?a advers?ria, ele a captura. Nesse caso, vai
		 * parar em uma casa vazia e a captura ter? que ser manual.
		 */
		// Testa se a pe?a movida ? uma inst?ncia de PE?O.
		if (p instanceof Pawn) {
			// Se a coluna de origem ? diferente da coluna de destino e a pe?a capturada
			// igual a nulo, ? um En Passant.
			if (source.getColumn() != target.getColumn() && capturedPiece == enPassantVulnerable) {
				/*Ser? necess?rio pegar a pe?a que sofreu o passante e que voltou para a posi??o de origem e lev?-la de volta para a 
				  posi??o de destino.*/
				ChessPiece pawn = (ChessPiece)board.removePiece(target);
				
				
				Position pawnPosition;
				if (p.getColor() == Color.WHITE) {
					pawnPosition = new Position(3, target.getColumn());
				} else {
					pawnPosition = new Position(4, target.getColumn());
				}
				//Coloca a pe?a que estava no lugar errado na posi??o que deve ficar.
				board.placePiece(pawn, pawnPosition);
				
				//N?o ? mais necess?rio fazer a troca de listas. Ela j? foi feita no in?cio do c?digo undoMove().
				/*capturedPiece = board.removePiece(pawnPosition);
				capturedPieces.add(capturedPiece);
				piecesOnTheBoard.remove(capturedPiece);*/
			}
		}

	}

	// Valida??o da pe?a de origem com duas verifica??es.
	private void validateSourcePosition(Position position) {
		if (!board.thereIsAPiece(position)) {
			throw new ChessException("There is no piece on source position!");
		}
		/*
		 * Terceira verifica??o: Se a pe?a atual for do advers?rio, n?o poder? ser
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

	// Varre o tabuleiro procurando uma pe?a que possa amea?ar o REI.
	private boolean testCheck(Color color) {
		// Pega a posi??o do REI no formato de matriz
		Position kingPosition = king(color).getChessPosition().toPosition();
		// Lista de pe?as do oponente do REI.
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
		// Se a cor que vier por par?metro n?o estiver em xeque, retorna FALSE.
		if (!testCheck(color)) {
			return false;
		}
		// Todas as pe?as da cor do par?metro ser?o filtradas nessa lista.
		List<Piece> list = piecesOnTheBoard.stream().filter(x -> ((ChessPiece) x).getColor() == color)
				.collect(Collectors.toList());

		for (Piece p : list) { // Percorre todas as pe?as que est?o na lista.
			boolean[][] mat = p.possibleMoves(); // Essa armazena os movimentos poss?veis.
			for (int i = 0; i < board.getRows(); i++) { // Percorre as linhas da matriz.
				for (int j = 0; j < board.getColumns(); j++) { // Percorre as colunas da matriz.
					if (mat[i][j]) { // Se encontrar um movimento poss?vel...
						Position source = ((ChessPiece) p).getChessPosition().toPosition(); // Move a pe?a "p" para o
																							// movimento poss?vel.
						Position target = new Position(i, j); // Posi??o de destino.
						Piece capturedPiece = makeMove(source, target); // Pe?a capturada ? substitu?da pela pe?a
																		// captora. Esse movimento foi para testar.
						boolean testCheck = testCheck(color); // Testa se o REI da cor do par?metro est? em cheque
						undoMove(source, target, capturedPiece); // Desfaz o movimento.
						if (!testCheck) { // Se n?o est? em xeque, retorna FALSE, pois n?o est? em xeque-mate.
							return false;
						}
					}
				}
			}
		}
		return true; // Depois que executar o FOR e n?o for encontrado nenhum movimento que saia do
						// check, retorna true.
	}

	private void placeNewPiece(char column, int row, ChessPiece piece) {
		board.placePiece(piece, new ChessPosition(column, row).toPosition()); // Al?m de colocar a pe?a no tabuleiro...
		// ...j? coloca tamb?m na lista de pe?as do tabuleiro.
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
		placeNewPiece('a', 2, new Pawn(board, Color.WHITE, this));
		placeNewPiece('b', 2, new Pawn(board, Color.WHITE, this));
		placeNewPiece('c', 2, new Pawn(board, Color.WHITE, this));
		placeNewPiece('d', 2, new Pawn(board, Color.WHITE, this));
		placeNewPiece('e', 2, new Pawn(board, Color.WHITE, this));
		placeNewPiece('f', 2, new Pawn(board, Color.WHITE, this));
		placeNewPiece('g', 2, new Pawn(board, Color.WHITE, this));
		placeNewPiece('h', 2, new Pawn(board, Color.WHITE, this));

		placeNewPiece('a', 8, new Rook(board, Color.BLACK));
		placeNewPiece('b', 8, new Knight(board, Color.BLACK));
		placeNewPiece('c', 8, new Bishop(board, Color.BLACK));
		placeNewPiece('d', 8, new Queen(board, Color.BLACK));
		placeNewPiece('e', 8, new King(board, Color.BLACK, this));
		placeNewPiece('f', 8, new Bishop(board, Color.BLACK));
		placeNewPiece('g', 8, new Knight(board, Color.BLACK));
		placeNewPiece('h', 8, new Rook(board, Color.BLACK));
		placeNewPiece('a', 7, new Pawn(board, Color.BLACK, this));
		placeNewPiece('b', 7, new Pawn(board, Color.BLACK, this));
		placeNewPiece('c', 7, new Pawn(board, Color.BLACK, this));
		placeNewPiece('d', 7, new Pawn(board, Color.BLACK, this));
		placeNewPiece('e', 7, new Pawn(board, Color.BLACK, this));
		placeNewPiece('f', 7, new Pawn(board, Color.BLACK, this));
		placeNewPiece('g', 7, new Pawn(board, Color.BLACK, this));
		placeNewPiece('h', 7, new Pawn(board, Color.BLACK, this));
	}
}
