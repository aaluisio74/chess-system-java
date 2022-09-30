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
	
	//A aplica��o n�o pode pegar a posi��o da pe�a no tabuleiro. Mas, pode ter acesso �s suas coordenadas.
	public ChessPosition getChessPosition() {
		// O m�todo fromPosition converte a pe�a de xadrez no tabuleiro para a posi��o no tabuleiro (coordenadas).
		return ChessPosition.fromPosition(position);
	}
	
	//PROTECTED para ser acess�vel apenas no pacote (chess) e pelas subclasses (chess.piece) das pe�as (King e Rook)
	protected boolean isThereOpponentPiece(Position position) {
		/*Recebe a pe�a que est� na posi��o do par�metro. Aqui teremos um downcasting: � quando o objeto se passa
		  como se fosse um subtipo dele. Desce a hierarquia.
		  Upcasting: � quando uma superclasse recebe uma refer�ncia da subclasse. Sobe a hierarquia.*/
		ChessPiece p = (ChessPiece)getBoard().piece(position);
		//Retorna se o objeto for diferente de nulo e se a cor da pe�a � diferente da cor da pe�a advers�ria.
		return p != null && p.getColor() != color;
	}
}
