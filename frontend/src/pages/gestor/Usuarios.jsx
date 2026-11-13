import { useEffect, useMemo, useState } from 'react'
import { api } from '../../api.js'
import { Alert, Badge, Card, confirmar, Empty, Field, Loading, Modal } from '../../components/ui.jsx'

const PERFIL = {
  GESTOR: ['Gestão / Secretaria', 'purple'],
  PROFESSOR: ['Professor(a)', 'blue'],
  ALUNO: ['Aluno(a)', 'green'],
  RESPONSAVEL: ['Responsável', 'amber'],
}
// Perfis que podem ser criados por aqui (aluno é gerado em Matrículas).
const CRIAVEIS = [['GESTOR', 'Gestão / Secretaria'], ['PROFESSOR', 'Professor(a)'], ['RESPONSAVEL', 'Responsável']]
const VAZIO = { nome: '', email: '', perfil: 'PROFESSOR', telefone: '', disciplinas: '' }

export default function Usuarios() {
  const [usuarios, setUsuarios] = useState(null)
  const [busca, setBusca] = useState('')
  const [filtro, setFiltro] = useState('')
  const [novo, setNovo] = useState(null)
  const [edit, setEdit] = useState(null)
  const [msg, setMsg] = useState({})

  const carregar = () => api.get('/gestor/usuarios').then(setUsuarios)
  useEffect(() => { carregar() }, [])

  const lista = useMemo(() => (usuarios || []).filter((u) => {
    const q = busca.trim().toLowerCase()
    return (!filtro || u.perfil === filtro)
      && (u.nome.toLowerCase().includes(q) || u.email.toLowerCase().includes(q))
  }), [usuarios, busca, filtro])

  const acao = async (fn, sucesso) => {
    setMsg({})
    try { const r = await fn(); setMsg({ ok: sucesso || r?.mensagem }); carregar(); return r } catch (e) { setMsg({ erro: e.message }) }
  }

  const criar = async () => {
    if (await acao(() => api.post('/gestor/usuarios', novo), 'Usuário criado (senha inicial 123456).')) setNovo(null)
  }
  const salvar = async () => {
    if (await acao(() => api.put(`/gestor/usuarios/${edit.id}`, edit), 'Usuário atualizado.')) setEdit(null)
  }
  const resetar = async (u) => {
    if (!(await confirmar(`Redefinir a senha de ${u.nome} para a senha inicial 123456?`))) return
    acao(() => api.post(`/gestor/usuarios/${u.id}/resetar-senha`), null)
  }

  if (!usuarios) return <Loading />
  return (
    <>
      <Alert tone="success" onClose={() => setMsg({})}>{!novo && !edit && msg.ok}</Alert>
      <Alert tone="error" onClose={() => setMsg({})}>{!novo && !edit && msg.erro}</Alert>
      <Card>
        <div className="toolbar">
          <p className="muted small">{lista.length} de {usuarios.length} usuário(s)</p>
          <button className="btn btn-primary" onClick={() => { setNovo(VAZIO); setMsg({}) }}>+ Novo usuário</button>
        </div>
        <div className="filters">
          <input placeholder="🔍 Buscar por nome ou e-mail..." value={busca} onChange={(e) => setBusca(e.target.value)} />
          <select value={filtro} onChange={(e) => setFiltro(e.target.value)}>
            <option value="">Todos os perfis</option>
            {Object.entries(PERFIL).map(([k, [label]]) => <option key={k} value={k}>{label}</option>)}
          </select>
        </div>
        {lista.length === 0 ? <Empty /> : (
          <div className="table-wrap">
            <table className="table">
              <thead><tr><th>Nome</th><th>E-mail</th><th>Perfil</th><th>Contato</th><th></th></tr></thead>
              <tbody>
                {lista.map((u) => (
                  <tr key={u.id}>
                    <td data-label="Nome"><strong>{u.nome}</strong></td>
                    <td data-label="E-mail">{u.email}</td>
                    <td data-label="Perfil"><Badge tone={PERFIL[u.perfil][1]}>{PERFIL[u.perfil][0]}</Badge></td>
                    <td data-label="Contato">
                      {u.telefone || '—'}
                      {u.perfil === 'PROFESSOR' && u.disciplinas && <div className="muted small">{u.disciplinas}</div>}
                    </td>
                    <td className="r actions-cell">
                      <button className="btn btn-sm btn-ghost" onClick={() => { setEdit({ ...u, telefone: u.telefone ?? '', disciplinas: u.disciplinas ?? '' }); setMsg({}) }}>Editar</button>
                      <button className="btn btn-sm btn-ghost" onClick={() => resetar(u)}>Resetar senha</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </Card>

      {novo && (
        <Modal title="Novo usuário" onClose={() => { setNovo(null); setMsg({}) }} footer={<>
          <button className="btn btn-ghost" onClick={() => setNovo(null)}>Cancelar</button>
          <button className="btn btn-primary" onClick={criar}>Criar usuário</button>
        </>}>
          <Alert tone="error">{msg.erro}</Alert>
          <div className="form-grid">
            <Field label="Nome completo" full><input value={novo.nome} onChange={(e) => setNovo({ ...novo, nome: e.target.value })} /></Field>
            <Field label="E-mail (login)"><input type="email" value={novo.email} onChange={(e) => setNovo({ ...novo, email: e.target.value })} /></Field>
            <Field label="Perfil">
              <select value={novo.perfil} onChange={(e) => setNovo({ ...novo, perfil: e.target.value })}>
                {CRIAVEIS.map(([k, label]) => <option key={k} value={k}>{label}</option>)}
              </select>
            </Field>
            <Field label="Telefone"><input value={novo.telefone} onChange={(e) => setNovo({ ...novo, telefone: e.target.value })} /></Field>
            {novo.perfil === 'PROFESSOR' && (
              <Field label="Disciplinas de atuação"><input value={novo.disciplinas} onChange={(e) => setNovo({ ...novo, disciplinas: e.target.value })} /></Field>
            )}
          </div>
          <p className="muted small">O usuário é criado com a senha inicial <strong>123456</strong>. Para dar acesso a um <strong>aluno</strong>, use a tela de Matrículas (ficha de um aluno matriculado).</p>
        </Modal>
      )}

      {edit && (
        <Modal title={`Editar — ${edit.nome}`} onClose={() => { setEdit(null); setMsg({}) }} footer={<>
          <button className="btn btn-ghost" onClick={() => setEdit(null)}>Cancelar</button>
          <button className="btn btn-primary" onClick={salvar}>Salvar alterações</button>
        </>}>
          <Alert tone="error">{msg.erro}</Alert>
          <div className="form-grid">
            <Field label="Nome completo" full><input value={edit.nome} onChange={(e) => setEdit({ ...edit, nome: e.target.value })} /></Field>
            <Field label="E-mail (login)"><input type="email" value={edit.email} onChange={(e) => setEdit({ ...edit, email: e.target.value })} /></Field>
            <Field label="Perfil"><input disabled value={PERFIL[edit.perfil][0]} /></Field>
            <Field label="Telefone"><input value={edit.telefone} onChange={(e) => setEdit({ ...edit, telefone: e.target.value })} /></Field>
            {edit.perfil === 'PROFESSOR' && (
              <Field label="Disciplinas de atuação"><input value={edit.disciplinas} onChange={(e) => setEdit({ ...edit, disciplinas: e.target.value })} /></Field>
            )}
          </div>
          <p className="muted small">O perfil não pode ser alterado. Use “Resetar senha” na lista para voltar a senha deste usuário para 123456.</p>
        </Modal>
      )}
    </>
  )
}
