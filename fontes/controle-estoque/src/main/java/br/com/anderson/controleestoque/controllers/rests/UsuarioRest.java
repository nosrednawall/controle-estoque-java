package br.com.anderson.controleestoque.controllers.rests;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.OptimisticLockException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;

import br.com.anderson.controleestoque.controllers.validators.UsuarioValidator;
import br.com.anderson.controleestoque.model.dao.UsuarioDao;
import br.com.anderson.controleestoque.model.entity.Usuario;
import br.com.anderson.controleestoque.model.enums.StatusBinarioEnum;

/**
 * 
 * @author anderson
 *
 */
@Stateless
@Path("/usuarios")
public class UsuarioRest {

	/**
	 * Dao sendo injetado pelo CDI
	 */
	@Inject
	private UsuarioDao dao;

	/**
	 * Método para adicionar um usuario
	 * 
	 * @param usuarioGson
	 * @return
	 */
	@POST
	@Produces("application/json")
	@Consumes("application/json")
	public Response create(Usuario entity) {

		UsuarioValidator validacao = new UsuarioValidator();

		if (validacao.validaUsuario(entity)) {
			//não consigo comparar o hash criado aqui com o do login
			// try {
			// CriptografarSenhaUtil crypt = new CriptografarSenhaUtil();
			// String senha;
			// senha = crypt.createHash(entity.getSenha());
			// entity.setSenha(senha);
			// } catch (CannotPerformOperationException e) {
			// System.out.println("[ERRO] problema ao criptografar senha " + e);
			// // e.printStackTrace();
			// }

			dao.save(entity);
			return Response
					.created(UriBuilder.fromResource(UsuarioRest.class).path(String.valueOf(entity.getId())).build())
					.build();
		}
		System.out.println("[INFO] NÃO FOI POSSIVEL VALIDAR O USUÁRIO");
		return Response.status(Status.CONFLICT).entity(entity).build();
	}

	@GET
	@Path("/{id:[0-9][0-9]*}")
	@Produces("application/json")
	public Response buscaPorId(@PathParam("id") Integer id) {
		Usuario entity;

		try {
			entity = dao.find(Usuario.class, id);
		} catch (NoResultException nre) {
			entity = null;
		}
		if (entity == null) {
			return Response.status(Status.NOT_FOUND).build();
		}

		return Response.ok(entity).build();
	}

	@GET
	@Path("/listar/{status:[0-2]*}")
	@Produces("application/json")
	public List<Usuario> listaUsuarios(@PathParam("status") Integer idstatus) {

		StatusBinarioEnum status;

		final List<Usuario> results;

		if (idstatus == 0) {
			status = StatusBinarioEnum.ATIVO;
		} else if (idstatus == 1) {
			status = StatusBinarioEnum.INATIVO;
		} else {
			status = StatusBinarioEnum.AMBOS;
		}

		results = dao.listaTodos(status);

		return results;
	}

	@PUT
	@Path("/{id:[0-9][0-9]*}")
	@Consumes("application/json")
	public Response update(@PathParam("id") Integer id, Usuario entity) {
		if (entity == null) {
			return Response.status(Status.BAD_REQUEST).build();
		}
		if (id == null) {
			return Response.status(Status.BAD_REQUEST).build();
		}
		if (!id.equals(entity.getId())) {
			return Response.status(Status.CONFLICT).entity(entity).build();
		}
		if (dao.find(id) == null) {
			return Response.status(Status.NOT_FOUND).build();
		}
		try {
			entity = dao.update(entity);
		} catch (OptimisticLockException e) {
			return Response.status(Response.Status.CONFLICT).entity(e.getEntity()).build();
		}
		entity = dao.update(entity);
		return Response.noContent().build();
	}
}