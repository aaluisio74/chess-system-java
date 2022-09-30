package chess;

import boardgame.Board;
import boardgame.Piece;
import boardgame.Position;

public abstract class ChessPiece extends Piece {

	private Color color;

	public ChessPiece(Board board, Color color) {
		super(board);
		this.color = color;
	}

	public Color getColor() {
		return color;
	}
	
	//A aplicação não pode pegar a posição da peça no tabuleiro. Mas, pode ter acesso às suas coordenadas.
	public ChessPosition getChessPosition() {
		// O método fromPosition converte a peça de xadrez no tabuleiro para a posição no tabuleiro (coordenadas).
		return ChessPosition.fromPosition(position);
	}
	
	//PROTECTED para ser acessível apenas no pacote (chess) e pelas subclasses (chess.piece) das peças (King e Rook)
	protected boolean isThereOpponentPiece(Position position) {
		/*Recebe a peça que está na posição do parâmetro. Aqui teremos um downcasting: é quando o objeto se passa
		  como se fosse um subtipo dele. Desce a hierarquia.
		  Upcasting: é quando uma superclasse recebe uma referência da subclasse. Sobe a hierarquia.*/
		ChessPiece p = (ChessPiece)getBoard().piece(position);
		//Retorna se o objeto for diferente de nulo e se a cor da peça é diferente da cor da peça adversária.
		return p != null && p.getColor() != color;
	}
}
