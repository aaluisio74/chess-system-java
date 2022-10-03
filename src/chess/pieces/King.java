package chess.pieces;

import boardgame.Board;
import boardgame.Position;
import chess.ChessMatch;
import chess.ChessPiece;
import chess.Color;

public class King extends ChessPiece {

	//Uma dependência para a partida.
	private ChessMatch chessMatch;
	
	//Inicializa o chessMatch
	public King(Board board, Color color, ChessMatch chessMatch) {
		super(board, color);
		this.chessMatch = chessMatch;
	}

	@Override
	public String toString() {
		return "K";
	}

	// Implementação dos movimentos possíveis do Rei
	private boolean canMove(Position position) {
		ChessPiece p = (ChessPiece) getBoard().piece(position);
		//Ou a casa está vazia ou tem uma peça adversária.
		return p == null || p.getColor() != getColor();
	}
	
	/*#Special Move castling (ROQUE)
	 * Recebe a posição da TORRE e determina se está apta a executar o ROQUE.
	 * Estará apta quando a quantidade de movimentos da TORRE for zero.*/
	private boolean testRockCastling(Position position) {
		ChessPiece p = (ChessPiece)getBoard().piece(position);
		return p != null && p instanceof Rook && p.getColor() == getColor() && p.getMoveCount() == 0;
	}

	@Override
	public boolean[][] possibleMoves() {
		// Matriz de booleanos da mesma dimensão do tabuleiro. Por padrão, todas as
		// posições começam com false.
		boolean[][] mat = new boolean[getBoard().getRows()][getBoard().getColumns()];
		Position p = new Position(0, 0);

		// above
		p.setValues(position.getRow() - 1, position.getColumn());
		if (getBoard().positionExists(p) && canMove(p)) {
			mat[p.getRow()][p.getColumn()] = true;
		}

		// below
		p.setValues(position.getRow() + 1, position.getColumn());
		if (getBoard().positionExists(p) && canMove(p)) {
			mat[p.getRow()][p.getColumn()] = true;
		}

		// left
		p.setValues(position.getRow(), position.getColumn() - 1);
		if (getBoard().positionExists(p) && canMove(p)) {
			mat[p.getRow()][p.getColumn()] = true;
		}

		// right
		p.setValues(position.getRow(), position.getColumn() + 1);
		if (getBoard().positionExists(p) && canMove(p)) {
			mat[p.getRow()][p.getColumn()] = true;
		}

		// nw (northwest)
		p.setValues(position.getRow() - 1, position.getColumn() - 1);
		if (getBoard().positionExists(p) && canMove(p)) {
			mat[p.getRow()][p.getColumn()] = true;
		}

		// ne (north east)
		p.setValues(position.getRow() - 1, position.getColumn() + 1);
		if (getBoard().positionExists(p) && canMove(p)) {
			mat[p.getRow()][p.getColumn()] = true;
		}

		// sw (south-west)
		p.setValues(position.getRow() + 1, position.getColumn() - 1);
		if (getBoard().positionExists(p) && canMove(p)) {
			mat[p.getRow()][p.getColumn()] = true;
		}

		// se (southeast)
		p.setValues(position.getRow() + 1, position.getColumn() + 1);
		if (getBoard().positionExists(p) && canMove(p)) {
			mat[p.getRow()][p.getColumn()] = true;
		}

		//#Special Move castling (ROQUE)
		if (getMoveCount() == 0 && !chessMatch.getCheck()) {
			//#Special Move castling kingside rook
			//Guarda a posição do REI para o ROQUE PEQUENO.
			Position posT1 = new Position(position.getRow(), position.getColumn() + 3);
			if (testRockCastling(posT1)) {
				Position p1 = new Position(position.getRow(), position.getColumn() + 1);
				Position p2 = new Position(position.getRow(), position.getColumn() + 2);
				
				/*Se na posição p1 e p2 for nulo, pode fazer o ROQUE PEQUENO pois todas as
				  condições foram satisfeitas.*/
				if (getBoard().piece(p1) == null && getBoard().piece(p2) == null) {
					// Nova posição do REI.
					mat[position.getRow()][position.getColumn() + 2] = true;
				}
			}
			
			//#Special Move castling queenside rook
			//Guarda a posição do REI para o ROQUE GRANDE.
			Position posT2 = new Position(position.getRow(), position.getColumn() - 4);
			//Testa a posição da TORRE para ver se ela está apta para ROQUE.
			if (testRockCastling(posT2)) {
				Position p1 = new Position(position.getRow(), position.getColumn() - 1);
				Position p2 = new Position(position.getRow(), position.getColumn() - 2);
				Position p3 = new Position(position.getRow(), position.getColumn() - 3);
				
				/*Se na posição p1 e p2 for nulo, pode fazer o ROQUE PEQUENO pois todas as
				  condições foram satisfeitas.*/
				if (getBoard().piece(p1) == null && getBoard().piece(p2) == null && getBoard().piece(p3) == null) {
					// Nova posição do REI.
					mat[position.getRow()][position.getColumn() - 2] = true;
				}
			}
		}
		
		return mat;
	}
}