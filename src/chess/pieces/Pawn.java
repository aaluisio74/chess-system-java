package chess.pieces;

import boardgame.Board;
import boardgame.Position;
import chess.ChessMatch;
import chess.ChessPiece;
import chess.Color;

public class Pawn extends ChessPiece {

	// Dependência do PEÃO para a partida. Essa é a associação entre os objetos.
	private ChessMatch chessMatch;

	public Pawn(Board board, Color color, ChessMatch chessMatch) {
		super(board, color);
		this.chessMatch = chessMatch;
	}

	// Add constructor "Pawn(Board, Color)"
	public Pawn(Board board, Color color) {
		super(board, color);
	}

	// Add unimplemented methods
	@Override
	public boolean[][] possibleMoves() {
		// Matriz e posição copiados da TORRE
		boolean[][] mat = new boolean[getBoard().getRows()][getBoard().getColumns()];

		Position p = new Position(0, 0);
		// Código para o PEÃO BRANCO
		// Se cor do PEÃO for branca deve movimentar a peça para cima.
		if (getColor() == Color.WHITE) {
			// Testa se o peão pode se mover para cima.
			p.setValues(position.getRow() - 1, position.getColumn());
			// Se a posição exisitir e estiver vazia pode movimentar o peão
			if (getBoard().positionExists(p) && !getBoard().thereIsAPiece(p)) {
				mat[p.getRow()][p.getColumn()] = true;
			}
			// Outra regra é poder mover duas linhas a frente.
			p.setValues(position.getRow() - 2, position.getColumn());
			// Regra para verificar se a primeira casa da frente está vazia também
			Position p2 = new Position(position.getRow() - 1, position.getColumn());

			/*
			 * Se a posição exisitir e estiver vazia pode movimentar o peão Também testa se
			 * a contagem do movimento é igual a ZERO
			 */
			if (getBoard().positionExists(p) && !getBoard().thereIsAPiece(p) && getBoard().positionExists(p2)
					&& !getBoard().thereIsAPiece(p2) && getMoveCount() == 0) {
				mat[p.getRow()][p.getColumn()] = true;
			}

			// Regra para tomar a peça do adversário.
			p.setValues(position.getRow() - 1, position.getColumn() - 1);
			// Se a posição exisitir e estiver vazia pode movimentar o peão
			if (getBoard().positionExists(p) && isThereOpponentPiece(p)) {
				mat[p.getRow()][p.getColumn()] = true;
			}

			p.setValues(position.getRow() - 1, position.getColumn() + 1);
			// Se a posição exisitir e estiver vazia pode movimentar o peão
			if (getBoard().positionExists(p) && isThereOpponentPiece(p)) {
				mat[p.getRow()][p.getColumn()] = true;
			}

			// #Special move - En Passant da peça BRANCA
			if (position.getRow() == 3) { // Posição da peça na linha 3
				// Guarda a posição à esquerda
				Position left = new Position(position.getRow(), position.getColumn() - 1);
				// Testa se a posição da esquerda existe. Se tem uma peça que é oponente. E se a
				// peça no local está vulnerável a tomar o En Passant.
				if (getBoard().positionExists(left) && isThereOpponentPiece(left)
						&& getBoard().piece(left) == chessMatch.getEnPassantVulnerable()) {
					mat[left.getRow() - 1][left.getColumn()] = true; //Peão branco anda para cima, na matriz é negativo.
				}

				// Guarda a posição à direita
				Position right = new Position(position.getRow(), position.getColumn() + 1);
				// Testa se a posição da esquerda existe. Se tem uma peça que é oponente. E se a
				// peça no local está vulnerável a tomar o En Passant.
				if (getBoard().positionExists(right) && isThereOpponentPiece(right)
						&& getBoard().piece(right) == chessMatch.getEnPassantVulnerable()) {
					mat[right.getRow() - 1][right.getColumn()] = true;
				}
			}

		} else {
			// Mesmo código acima para o PEÃO PRETO
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

			// #Special move - En Passant da peça PRETA
			if (position.getRow() == 4) { // Posição da peça na linha 4. De cima para baixo.
				// Guarda a posição à esquerda
				Position left = new Position(position.getRow(), position.getColumn() - 1);
				// Testa se a posição da esquerda existe. Se tem uma peça que é oponente. E se a
				// peça no local está vulnerável a tomar o En Passant.
				if (getBoard().positionExists(left) && isThereOpponentPiece(left)
						&& getBoard().piece(left) == chessMatch.getEnPassantVulnerable()) {
					mat[left.getRow() + 1][left.getColumn()] = true; //Peão preto anda para baixo, na matriz é positivo.
				}

				// Guarda a posição à direita
				Position right = new Position(position.getRow(), position.getColumn() + 1);
				// Testa se a posição da esquerda existe. Se tem uma peça que é oponente. E se a
				// peça no local está vulnerável a tomar o En Passant.
				if (getBoard().positionExists(right) && isThereOpponentPiece(right)
						&& getBoard().piece(right) == chessMatch.getEnPassantVulnerable()) {
					mat[right.getRow() + 1][right.getColumn()] = true;
				}
			}

		}
		return mat;
	}

	@Override
	public String toString() {
		return "P";
	}
}
