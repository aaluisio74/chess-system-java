package chess.pieces;

import boardgame.Board;
import boardgame.Position;
import chess.ChessPiece;
import chess.Color;

public class Pawn extends ChessPiece {

	// Add constructor "Pawn(Board, Color)"
	public Pawn(Board board, Color color) {
		super(board, color);
	}

	// Add unimplemented methods
	@Override
	public boolean[][] possibleMoves() {
		// Matriz e posi��o copiados da TORRE
		boolean[][] mat = new boolean[getBoard().getRows()][getBoard().getColumns()];

		Position p = new Position(0, 0);
		//C�digo para o PE�O BRANCO
		// Se cor do PE�O for branca deve movimentar a pe�a para cima.
		if (getColor() == Color.WHITE) {
			// Testa se o pe�o pode se mover para cima.
			p.setValues(position.getRow() - 1, position.getColumn());
			// Se a posi��o exisitir e estiver vazia pode movimentar o pe�o
			if (getBoard().positionExists(p) && !getBoard().thereIsAPiece(p)) {
				mat[p.getRow()][p.getColumn()] = true;
			}
			// Outra regra � poder mover duas linhas a frente.
			p.setValues(position.getRow() - 2, position.getColumn());
			// Regra para verificar se a primeira casa da frente est� vazia tamb�m
			Position p2 = new Position(position.getRow() - 1, position.getColumn());

			/*
			 * Se a posi��o exisitir e estiver vazia pode movimentar o pe�o Tamb�m testa se
			 * a contagem do movimento � igual a ZERO
			 */
			if (getBoard().positionExists(p) && !getBoard().thereIsAPiece(p) && getBoard().positionExists(p2)
					&& !getBoard().thereIsAPiece(p2) && getMoveCount() == 0) {
				mat[p.getRow()][p.getColumn()] = true;
			}

			// Regra para tomar a pe�a do advers�rio.
			p.setValues(position.getRow() - 1, position.getColumn() - 1);
			// Se a posi��o exisitir e estiver vazia pode movimentar o pe�o
			if (getBoard().positionExists(p) && isThereOpponentPiece(p)) {
				mat[p.getRow()][p.getColumn()] = true;
			}

			p.setValues(position.getRow() - 1, position.getColumn() + 1);
			// Se a posi��o exisitir e estiver vazia pode movimentar o pe�o
			if (getBoard().positionExists(p) && isThereOpponentPiece(p)) {
				mat[p.getRow()][p.getColumn()] = true;
			}
		} else {
			//Mesmo c�digo acima para o PE�O PRETO
			p.setValues(position.getRow() + 1, position.getColumn());

			if (getBoard().positionExists(p) && !getBoard().thereIsAPiece(p)) {
				mat[p.getRow()][p.getColumn()] = true;
			}
			
			p.setValues(position.getRow() + 2, position.getColumn());
			Position p2 = new Position(position.getRow() + 1, position.getColumn());

			if (getBoard().positionExists(p) && !getBoard().thereIsAPiece(p) && getBoard().positionExists(p2)
					&& !getBoard().thereIsAPiece(p2) && getMoveCount() == 0) {
				mat[p.getRow()][p.getColumn()] = true;
			}

			p.setValues(position.getRow() + 1, position.getColumn() - 1);
			if (getBoard().positionExists(p) && isThereOpponentPiece(p)) {
				mat[p.getRow()][p.getColumn()] = true;
			}

			p.setValues(position.getRow() + 1, position.getColumn() + 1);
			if (getBoard().positionExists(p) && isThereOpponentPiece(p)) {
				mat[p.getRow()][p.getColumn()] = true;
			}
		}
		return mat;
	}
	@Override
	public String toString() {
		return "P";
	}	
}
